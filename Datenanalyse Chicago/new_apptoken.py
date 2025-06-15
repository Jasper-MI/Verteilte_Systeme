import requests
import time

url = "https://data.cityofchicago.org/resource/ijzp-q8t2.json"
app_token = "c5bA1X3b83QjEZaoATn0hGsG2"


query = {
    "$select": "block, count(*) as count_crimes",
    "$where": "year = 2024",
    "$group": "block",
    "$order": "count_crimes DESC",
    "$limit": "10"
}


headers = {
    "X-App-Token": app_token
}

#startzeit
start_time = time.time()

#daten collecten
r = requests.get(url, params=query, headers=headers)

#zeit gesamt == differenz startzeitpunkt <-> endzeitpunkt
end_time = time.time()
download_time = end_time - start_time

status = r.status_code

if status == 200:
    data = r.json()
    
    block_num = 1

    for item in data:
        block = item.get("block")
        count = item.get("count_crimes")
        print(f"{block_num} Block: {block}, Crimes: {count}")
        block_num += 1

    print(f"\nDauer des Daten-Downloads: {download_time:.3f} Sekunden")
else:
    print(f"Fehler beim Abrufen der Daten. Status Code: {status}")