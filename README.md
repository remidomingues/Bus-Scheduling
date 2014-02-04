BusScheduling
=============
Presentation
------------
The development of this software was supervised by Mr. Claudio Cesar De Sá
and Mr Lucas Hermann Negri. Mr Philippe Kauffman was the tutor of this
internship.

Developed by Mr. Rémi Domingues and Mr. Yoann Alvarez, this project aimed
at creating an application which could calculate bus schedules under
constraints, such as time frames, locations order and bus sits.

The calculated schedule has to cross every geographic location specified by
the user. The schedule duration had also to be as short as possible. In
addition, passengers had to be taken on board the bus before being dropped
off. By the way, the bus capacities had obviously to be taken into account.


Requirements
------------
This project is compatible with Java 1.6

A MySQL database is also required :
- Login : gidion
- Password : gi7dw4

Initialization of the <gidion> database and tables :
- Execute gidionInit.sql from the maintenance folder

Database data from Joinville, Brazil :
- Execute gidionData.sql from the maintenance folder

Note :
This project uses the Google Geocoding API. This one provides limited accesses
and you may require a Professional account.


Execution
---------
Windows : launch the jar with the <Planificação do ônibus.bat> file
UNIX : Execute java -jar jar.jar when you are in the jar folder


Jar management
--------------
The jar must contains the pictures <gidion.png> and <onibus.png> at its root.
Please copy these two pictures, from the resources folder, inside the jar
after the export.
