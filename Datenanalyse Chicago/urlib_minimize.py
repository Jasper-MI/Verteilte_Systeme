import collections
import urllib.request
import urllib.parse
import json
import time

blocksArray = []

base_url = 'https://data.cityofchicago.org/resource/ijzp-q8t2.json?'

sql_query = {
    "$query": "SELECT block, COUNT(*) AS count WHERE date >= '2024-01-01T00:00:00' AND date <= '2025-01-01T00:00:00' GROUP BY block ORDER BY count DESC LIMIT 10"
}

print(base_url + urllib.parse.urlencode(sql_query))

# url = 'https://data.cityofchicago.org/resource/ijzp-q8t2.json?$query=SELECT%20block%20LIMIT%2010'

start = time.time()
# open the URL and read the JSON data
with urllib.request.urlopen(base_url + urllib.parse.urlencode(sql_query)) as response:
    html = response.read()
    end = time.time()
    json_data = json.loads(html)
    print(json.dumps(json_data, indent=2)) # print result

# print download time
print("download time", (end - start))
