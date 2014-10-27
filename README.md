# Projekt RemoteClassroom
## Einleitung
RemoteClassroom ist ein Projekt aus dem Informatikunterricht und stellt ein Tool dar, welches die Verwaltung von mehreren Computern in einem Netzwerk erlaubt. RemoteClassroom besteht aus einer Steuerungseinheit, dem Controller, und einer Client Applikation, welche auf den zu verwaltenden Rechnern im Hintergrund laufen soll.

## Entwicklung
RemoteClassroom ist vollst&auml;ndig in Java geschrieben. Entwickelt wird mit IntelliJ IDEA Community Edition. Das Repository muss als Projekt importiert werden, die Module "Controller", "Client" und "ClientUpdate" werden in der IDE als seperate Module entsprechend erkannt und angezeigt. Weiterhin ben&ouml;tigt der Controller den JDBC-SQLite Datenbanktreiber (Download https://bitbucket.org/xerial/sqlite-jdbc/downloads). F&uuml;r die Entwicklung wurde Version 3.7.2 verwendet. Der Treiber kann dem Controller dann via File&gt;Project Structure...&gt;Modules&gt;Controller&gt;Dependencies hinzugef&uuml;gt werden.

## Kompilierung
Folgend wird die Kompilierung f&uuml;r den Productivity Case innerhalb von IntelliJ IDEA Community Edition beschrieben. Folgender Artikel setzt die Ausf&uuml;rung der oben genannten Schritte vorraus.  
Die "Pakete" sind zun&auml;chst in den Projekt Artifacts zu konfigurieren. Gehen Sie daf&uuml;r zun&auml;chst auf File&gt;Project Structure...&gt;Artifacts und erstellen hier ein neues Artifact vom Typ Jar&gt;From modules with Dependencies... . W&auml;len Sie in dem Dialogfenster unter Module: Client aus, bei Main Class: ClassroomClient, und setzten Sie den Haken bei "copy to the output directory and link via manifest". Best&auml;tigen Sie anschlie&szlig;end mit OK und w&auml;len Sie Build on make aus.  
Erstellen Sie mit gleichen Einstellungen nun ein zweites Artifact, w&auml;hlen Sie als Module jedoch Controller und als Main Class Init aus.  
Gehen Sie danach wieder zur&uuml;ck auf Client:jar. Klicken Sie in dem Tab "Output Layout" auf "Create Archive". Nennen Sie das soeben erstellte Archiv "ClientUpdate.jar" (Case sensitive) und bewegen per Drag&amp;Drop "'ClientUpdate' compile output" in ClientUpdate.jar.  
Pr&uuml;fen Sie jetzt bei beiden eben erstellten Artifacts ob eine Manifest-Datei existiert. Sollte diese fehlen, wird ein Dialog zum Erstellen in der linken Spalte unter "Output Layout" angezeigt. In den meisten F&auml;llen stimmen die Pfade schon, best&auml;tigen Sie also einfach.  
Sollten Sie diese Schritte befolgt haben, k&ouml;nnen Sie den Project Structure-Dialog mit einem Klick auf "OK" quittieren. Um das Projekt nun zu kompilieren, w&auml;hlen Sie einfach Build&gt;Build Artifacts... im Men&uuml; an. Nachdem Sie mit All Artifacts&gt;Rebuild die Applikation erstellt haben, finden Sie die Binaries in Ihrem Projektordner&gt;out&gt;artifacts .

## Autoren
* Julien Schroeter
* Timo Bauer

## Installation
Anweisungen zur Installation finden Sie in der Datei INSTALL.md