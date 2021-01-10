# Points REST API

### Running

You need to download and install sbt for this application to run.

Once you have sbt installed, the following at the command prompt will start up Play in development mode:

```bash
sbt run
```

Play will start up on the HTTP port at <http://localhost:9000/>.   

### Usage

The following command executed from a Bash command line will show the amount of points the user currently has:
```bash
curl http://localhost:9000/v1/points
```
here is the response:

```routes
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100    50  100    50    0     0   1785      0 --:--:-- --:--:-- --:--:--  1851{"Dannon":1100,"Unilever":200,"MillerCoors":10000}
```

The following command executed from a Bash command line will deduct points from the users supply, and return the amount applied to each vendor:

```bash
curl --header "Content-Type: application/json" --request POST --data "{\"points\": 5000}" http://localhost:9000/v1/points/deduct
```
here is the response showing new totals for all vendors:

```routes
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100    66  100    50  100    16    543    173 --:--:-- --:--:-- --:--:--   725{"Dannon":-300,"Unilever":-200,"MillerCoors":-4500}
```

The following command executed from a Bash command line will show the amount of points the user currently has for a given vendor:
```bash
curl http://localhost:9000/v1/points/Dannon
```
here is the response:

```routes
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100    67  100    67    0     0   1175      0 --:--:-- --:--:-- --:--:--  1175{"vendor":"Dannon","points":1300,"timestamp":"2020-10-31T10:00:00"}
```

The following command executed from a Bash command line will add points to a users total:
```bash
curl --header "Content-Type: application/json" --request POST --data "{\"vendor\": \"Dannon\", \"points\": 5000}" http://localhost:9000/v1/points/add
```
here is the response showing new totals for all vendors:

```routes
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100    86  100    50  100    36   1612   1161 --:--:-- --:--:-- --:--:--  2866{"Dannon":6300,"Unilever":200,"MillerCoors":10000}
```
