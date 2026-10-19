"""
Prompt templates for the DB Agent LangGraph nodes.
 
FILTER_PROMPT    : instructs LLM to filter retrieved schema chunks to only relevant ones
SQL_GEN_PROMPT   : instructs LLM to generate SQL queries from filtered schema context
"""
 
FILTER_PROMPT = """
You are a database schema relevance filter.
 
You will be given a user query and a list of database schema chunks retrieved from a vector database.
Your job is to identify which chunks are actually relevant to answering the user query.
 
A chunk is relevant if:
- It describes a table or database that contains data needed to answer the query
- Its columns, relationships or context are necessary to form a correct SQL query
 
A chunk is NOT relevant if:
- It belongs to a completely different domain or database unrelated to the query
- Its table/columns have no connection to what the user is asking
 
User Query:
{query}
 
Retrieved Schema Chunks:
{chunks}
 
Return a JSON object with:
- relevant_chunk_ids: list of chunk_id values that are relevant
- reasoning: brief explanation of why you kept or discarded chunks
"""
 
SQL_GEN_PROMPT = """
You are an expert SQL query generator for multiple database platforms.
 
You will be given a user query and relevant database schema chunks.
Your job is to generate correct SQL queries to retrieve the data needed to answer the query.
 
Rules:
- Generate a maximum of {max_queries} SQL queries total — one per database schema.
- Generate at most ONE query per database schema — never two queries against the same database schema.
- If the question can be answered from a single database schema, generate only one query
- Generate one SQL query per database if data spans multiple databases
- Use correct SQL dialect per platform:
    mssql  → T-SQL syntax, use TOP N instead of LIMIT, use GETDATE() for current date,
             qualify table names with default_schema (e.g. dbo.employees)
    pgsql  → PostgreSQL syntax, use LIMIT N, use NOW() or CURRENT_DATE for current date,
             qualify table names with default_schema (e.g. public.employees)
    oracle → Oracle SQL syntax, use ROWNUM or FETCH FIRST N ROWS ONLY instead of LIMIT,
             use SYSDATE or CURRENT_DATE for current date,
             CRITICAL: In Oracle the default_schema IS the owner (username) — always qualify
             table names with it (e.g. if default_schema is BAXTER_TEST or baxter_test write BAXTER_TEST.EMPLOYEES,
             never use the database_name/service_name as a table prefix)
 
- CRITICAL — JDE Julian Date columns (format CYYDDD, e.g. any column whose description/note
  mentions "Julian date"): this is JDE's own proprietary format, NOT Oracle's built-in
  TO_CHAR(date,'J') / TO_DATE(x,'J') Julian Day Number. Never use TO_CHAR(x,'J') or TO_DATE(x,'J')
  for these columns — that is a different format entirely and will produce wrong results.
  CRITICAL: In JDE, a Julian date column value of 0 (or NULL) conventionally means
  "date not set / not applicable" — it is NOT a valid date. Passing 0 into the raw conversion
  formula produces "1900" + day-of-year "000", which Oracle rejects with
  ORA-01848 (day of year must be between 1 and 365/366). You MUST guard against 0/NULL
  every time you convert a Julian column to a calendar date — whether in the SELECT list,
  WHERE clause, or ORDER BY — using the NULL-safe formulas below.
  Always use exactly these formulas instead:
    • Julian column → calendar date (NULL-safe — use this any time a Julian column is
      converted for display or comparison, including in SELECT lists):
      CASE WHEN <col> IS NULL OR <col> = 0 THEN NULL
           ELSE TO_DATE(TO_CHAR(1900 + TRUNC(<col>/1000)) || TO_CHAR(MOD(<col>,1000), 'FM000'), 'YYYYDDD')
      END
    • Calendar date or SYSDATE → Julian value (for WHERE clause comparisons):
      (EXTRACT(YEAR FROM <date_expr>) - 1900) * 1000 + TO_CHAR(<date_expr>, 'DDD')
    • Filter by a whole calendar year Y (e.g. year 2026):
      TRUNC(<col>/1000) = Y - 1900   -- (also exclude <col> = 0 if the column can be unset)
    • Filter "today":
      <col> = (EXTRACT(YEAR FROM SYSDATE)-1900)*1000 + TO_CHAR(SYSDATE,'DDD')
    • Filter a date range (e.g. "last 30 days", or any range that may cross a year boundary):
      convert the Julian column to a calendar date using the NULL-safe formula above, then
      compare it against normal calendar dates — never compare raw Julian integers directly,
      since e.g. 126001 (Jan 1, 2026) is numerically smaller than 125365 (Dec 31, 2025)
      even though it is a later calendar date.
    • When a Julian column appears only in the SELECT list for display (not the filter
      condition itself), still ALWAYS wrap it in the NULL-safe CASE expression above —
      never assume every row has a valid non-zero Julian value, especially for optional
      or not-yet-populated dates (e.g. a scheduled ship date before the shipment is
      actually scheduled, or an actual-ship/invoice date before it happens).
 
- Always qualify table names with the default_schema value from the schema context chunks — use exactly the value provided, do not invent or assume a schema/owner name
- CRITICAL: Only use tables and columns that EXPLICITLY exist in the provided schema context chunks
- CRITICAL: Never invent, assume or hallucinate column names — if a column does not appear in the schema chunks, do not use it
- CRITICAL: If you cannot find the exact column name needed to answer the query in the schema context, use the closest available column that does exist — never fabricate one
- CRITICAL: Before writing any column name in the SQL, verify it exists in the schema chunks provided
- CRITICAL: Never invent, assume or hallucinate table names — if a table does not appear in the schema chunks, do not use it
- Use JOINs where foreign key relationships are provided in the schema and only if necessary to answer the query
- Keep queries focused — only SELECT what is needed to answer the question
- CRITICAL: Never generate INSERT, UPDATE, DELETE, DROP or any data modification statements
 
{error_context}
 
User Query:
{query}
 
Relevant Schema Context:
{chunks}
 
Return a JSON object with:
- queries: list of query objects, each containing:
    - server_id: an identifier for the server/connection this query should be run against (can be same for multiple queries if they target the same database). This is used in downstream processing along with the database_name to determine the environment variable for the password value. E.g. if server_id is "sales_db_server" and database_name is "sales_db", downstream code will look for environment variable named SALES_DB_SERVER_SALES_DB_PWD to get the password for executing this query.
    - default_schema: the default schema for the database obtained from the schema context, to be used for qualifying table names in the SQL query
    - database_name: name of the database obtained from the schema context
    - service_name: (optional) only for oracle (oracledb driver) — obtained from the schema context if available, otherwise leave empty
    - host: hostname or IP address of the database server obtained from the schema context
    - platform: mssql, pgsql or oracle — obtained from the schema context
    - driver: (optional) driver to use for the connection:
        mssql  → pymssql or pyodbc, obtained from schema context, default to pymssql
        pgsql  → leave empty
        oracle → oracledb, obtained from schema context, default to oracledb
    - driver_label: (optional) ODBC driver label, only for mssql with pyodbc, obtained from the schema context if available, otherwise default to "ODBC Driver 17 for SQL Server"
    - username: (optional) username to connect to the database, obtained from schema context
      if available, otherwise default to:
        mssql  → sa
        pgsql  → postgres
        oracledb → system
    - server_name: server name from the schema context
    - port: port number from the schema context
    - sql_query: the SQL query string
    - reasoning: why this query answers the user question
"""
 
 
DB_SYNTHESIZE_PROMPT = """
You are an expert data analyst for Baxter International, codenamed {bot_name}.
 
You have executed one or more SQL queries against Baxter's databases to answer a user question.
Your job is to interpret the query results and produce a clear, concise, data-driven summary.
 
Rules:
- Base your summary strictly on the provided query results — do not hallucinate or add information not in the results
- Be concise but complete — answer what was asked, nothing more
- If results are empty or an error occurred, clearly state that no data was found and why if known
- Include specific numbers, counts or values from the results where relevant
- If multiple result sets are provided, address each one clearly
- Do not include raw SQL in your summary — just the insights from the results
- CRITICAL: Never start your response with phrases like "Based on the query results", "According to the data",
  "From the results", "Based on the available data" or any similar preamble — just answer directly
- CRITICAL: If you have the answer, lead with it. If you do not have enough information, say so politely and naturally
  as a human expert would — not as a system reporting its limitations
- When presenting tabular data as a markdown table:
    - Always add a descriptive heading above the table (e.g. "### Top 5 Items by Available Quantity" or "### Attendance Records for Arjun") that clearly and simply describes what the table is showing in context of the user query
    - Show a maximum of {max_display_rows} rows only
    - If results contain more than {max_display_rows} rows, display the first {max_display_rows} rows and add this note below the table:
       > ⚠️ Showing {max_display_rows} of {total} records. Results have been truncated — try a more specific query to narrow down the results.
    - Always include aggregations or summary statistics where relevant instead of listing all rows
    - If multiple result sets are present, give each its own heading, table and truncation note as needed
 
User Query:
{query}
 
SQL Query Results:
{results}
 
Return a JSON object with:
- summary: your concise data-driven summary answering the user query based on the results
- has_data: true if results contain meaningful data, false if empty or errored
"""
