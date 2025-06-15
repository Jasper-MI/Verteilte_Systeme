import collections
import urllib.request
import json
import time


start = time.time()
# open the URL and read the JSON data
with urllib.request.urlopen('https://data.cityofchicago.org/resource/ijzp-q8t2.json') as response:
    html = response.read()
    end = time.time()
    json_data = json.loads(html)
    #print(json.dumps(json_data, indent=4))
    #print(json_data["block"])
    # https://stackoverflow.com/questions/12934699/selecting-fields-from-json-output
    # adding the block field to the blocksArray
    blocksArray = []
    counter = 0
    for item in json_data:
        blocksArray.append(item["block"])
        counter = counter + 1
        #print(item["block"])

print(counter)

#print(blocksArray)
# print the count of each block in the blocksArray
counts = collections.Counter(blocksArray)
new_list = sorted(blocksArray, key=lambda x: -counts[x])
new_list = list(dict.fromkeys(new_list))  # remove duplicates
for item in new_list[:10]:
    print(item, counts[item])

# print download time
print("download time", (end - start))