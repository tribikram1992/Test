import httpx
from typing import Optional
import logging
from pydantic import BaseModel
from core.config import settings

class GuardResponse(BaseModel):
    is_blocked: bool
    message: Optional[str] = None
    scanner_type: Optional[str] = None
    
blocking_response = "Regrettably, we are unable to process your request at this time as the provided input does not align with our content policy standards. We kindly ask you to consider rephrasing your prompt and resubmitting."

pii_response = "Regrettably, we are unable to process your request at this time as the provided input may include personal identifiable information and therefore does not align with our content standards. Please consider rephrasing your prompt and resubmitting."

class BaxiGuard:
    
    DEFAULT_SCANNERS = [
        {'type':'PromptInjection', 'block': True, 'message': blocking_response+" (x01)"},
        {'type':'Toxicity', 'block': True, 'message': blocking_response+" (x02)"},
        {'type':'Regex', 'block': True, 'message': blocking_response+" (x03)"},
        {'type':'Secrets', 'block': True, 'message': blocking_response+" (x04)"},
        {'type':'BanSubstrings', 'block':True, 'message': blocking_response+" (x05)"},
        {'type':'Anonymize', 'block': True, 'message': pii_response+" (x06)"}
    ]
    
    def __init__(self, active_scanners=None):
        self.scanners = active_scanners or self.DEFAULT_SCANNERS
        self.httpx_client = httpx.AsyncClient(timeout=httpx.Timeout(2.0, connect=1.0))
        self.guard_url = f"{settings.GUARDRAIL_ENDPOINT}/analyze/prompt"
        self.logger = logging.getLogger(__name__)

    async def check_prompt(self, query:str, app_name:Optional[str], requestor:Optional[str]) -> GuardResponse:
        try:
            response = await self.httpx_client.post(self.guard_url, json={"prompt": query, "app_name":app_name, "requestor": requestor})
            response.raise_for_status()
            
            result = response.json()
            if not result.get('is_valid'):
                self.logger.debug("Query blocked by Guard")
                for scanner in self.scanners:
                    if result['scanners'].get(scanner['type'], 0) > 0:
                        return GuardResponse(
                            is_blocked=True,
                            message=scanner['message'],
                            scanner_type=scanner['type']
                        ) 
            return GuardResponse(is_blocked=False)
        
        except (httpx.HTTPError, Exception):
            self.logger.error("LLM guard failed to validate request", exc_info=True)
            return GuardResponse(is_blocked=False)
