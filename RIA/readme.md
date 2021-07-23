<img src="TIW/logos/ebuy_plus.svg" width="20%"/>
<br><br>

# RIA
## Run the project
1. Clone this project.

2. Execute the ```.sql``` files inside the ```db_dump``` folder to create the DB. Files **must** be executed in the following order:
    * ```market_db.sql```
    * ```users.sql```
    * ```items.sql```
    * ```auctions.sql```
    * ```offers.sql```<br><br>

3. Change the following DB credentials according to your local DB instance: ```dbUser``` and ```dbPassword```, located inside ```EbuyPlus > webapp > WEB-INF > web.xml```.

4. Insert an absolute path for the items image location, it is located inside ```EbuyPlus > src > main > java > controller > utils > Values.java > IMAGE_FOLDER_PATH```.

    *NOTE: this folder must be placed outside the project folder.*

5. **IMPORTANT**: in order to allow Gson to properly parse ```java.time.LocalDateTime``` objects, the JVM must be run with the following flag: ```--add-opens=java.base/java.time=ALL-UNNAMED```.
To do so (in eclipse):
    - Right click on the project: ```Run As > Run Configurations...```
    - In the left panel select your ```Tomcat Server on Localhost```
    - Click on the ```Arguments``` tab
    - Scroll ```VM Arguments``` till the end
    - Append the above flag (there must be a **space** between the last word and the start of your flag).