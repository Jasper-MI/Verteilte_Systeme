import pandas as pd
from sodapy import Socrata
from getpass import getpass
import time

#passwordInput = getpass("Enter your password: ")

#client = Socrata("data.cityofchicago.org",
#                "x96rFbd0JguoobL2UEdECjo5w",
#                username="jasper.gruenbaum@gmail.com",
#                password= passwordInput)

client = Socrata("data.cityofchicago.org",
                "x96rFbd0JguoobL2UEdECjo5w")


sql_query = {
    "$query": "SELECT block, COUNT(*) AS count WHERE date >= '2024-01-01T00:00:00' AND date <= '2025-01-01T00:00:00' GROUP BY block ORDER BY count DESC LIMIT 10"
}

start = time.time()

result = client.get("ijzp-q8t2", **sql_query) # dataset identifier

end = time.time()

df = pd.DataFrame.from_records(result)
print(df)
print("download time", (end - start))