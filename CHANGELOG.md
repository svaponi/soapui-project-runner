SoapUI Project Runner
=======================
Questo file è usato per tracciare le modifiche fatte al progetto.


v1.1.0 - 2016/10/25
-----
- Aggiunte le dipendeze nel pom.xml
- Rimosse le user-libraries locali (`<soapui-home>/lib/\*.jar` e `<soapui-home>/bin/\*.jar`)
- Aggiunti Unit Tests su dei progetti SoapUI di prova (`<proj_home>/src/test/resources/dummy-test-project.xml` e `<proj_home>/src/test/resources/dummy-test-project_with-errors.xml`)


v1.0.3 - 2016/07/05
-----
- Rinominati i file di output con il timestamp come prefisso per facilitare l'ordinamento (in modo che stdout e risultati del test sia consecutivi) 


v1.0.2 - 2016/06/29
-----
- Aggiunta la entity TestCaseResult per salvare i risultati dei test
- Risultati scritti su file in un path relativo ==> `./output/<nome-progetto>__<timestamp>.log`
- Aggiunta la libreria Log4j anche per i log del Runner
- Modificato RootLogger affinché scriva su file in una directory in un path relativo ==> `./output/stdout__<timestamp>.log`


v1.01 - 2016/06/28
------
- Aggiunte le dipendenze ai plugin di SoapUI per evitare i ClassNotFoundException (analoghe a dipendenze in `<user-home>\.soapuios\plugin`)
	- `<root>/BundlePlugins/readyapi-swaggerhub-plugin-1.0.jar`
	- `<root>/BundlePlugins/ready-mqtt-plugin-dist.jar`
	- `<root>/BundlePlugins/ready-uxm-plugin-1.0.1-dist.jar`
	- `<root>/BundlePlugins/soapui-swagger-plugin-2.2-dist.jar`


