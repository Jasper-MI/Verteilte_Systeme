import pandas as pd
import json
import urllib.request
import urllib.parse
import time

base_url = "https://data.cityofchicago.org/resource/ijzp-q8t2.json?"

sql_query = {
    "$query": "SELECT block, COUNT(*) AS count",
    "$where": "date >= '2024-01-01T00:00:00' AND date <= '2025-01-01T00:00:00'",
    "$group": "block",
    "$order": "count DESC",
    "$limit": "10"
}


app_token = {
    "X-App-Token": "x96rFbd0JguoobL2UEdECjo5w"
}

request = urllib.request.Request(
    base_url,
    params=sql_query,
    headers=app_token
)


result = request.get(base_url, params=sql_query, headers=app_token)

end = time.time()
json_data = result.json()
print(json.dumps(json_data, indent=2))  # print result	