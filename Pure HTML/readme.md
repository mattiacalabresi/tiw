<img src="TIW/logos/ebuy.svg" width="20%"/>
<br><br>

# Pure HTML
## Run the project
1. Clone this project.

2. Execute the ```.sql``` files inside the ```db_dump``` folder to create the DB. Files **must** be executed in the following order:
    * ```market_db.sql```
    * ```users.sql```
    * ```items.sql```
    * ```auctions.sql```
    * ```offers.sql```<br><br>

3. Change the following DB credentials according to your local DB instance: ```dbUser``` and ```dbPassword```, located inside ```Ebuy > webapp > WEB-INF > web.xml```.

4. Insert an absolute path for the items image location, it is located inside ```Ebuy > src > main > java > controller > utils > Values.java > IMAGE_FOLDER_PATH```.
<br><br>
*NOTE: this folder must be placed outside the project folder.*