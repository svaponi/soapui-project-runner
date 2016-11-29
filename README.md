# soapui-project-runner
=====

### Usage

Per utilizzare il runner occore avere Java installato (1.6).

```shell
java -jar {name}.jar -p /path/to/soapui-project.xml \[-l level] \[-s suite1 suite2 ...] \[-c case1 suite2:case2 ...] 

  -p soapui-project-path      REQUIRED     file xml del progetto (path relativo o assoluto) 
  -l level                    OPTIONAL     livello di output, default è INFO. Altri: DEBUG (stampa tutto, inclusi i test disabilitati o ignorati); INFO e WARN (stampa solo i test eseguiti); ERROR (stanpa solo i test falliti) 
  -s suite1 suite2            OPTIONAL     lista di TestSuite da eseguire 
  -c case1 suite2:case2       OPTIONAL     lista di TestCase da eseguire (in caso di omonimia è possibile disambiguare i TestCase specificando la TestSuite come prefisso separata da ":") 
```

### Examples

Esempio di esecuzione

```shell
java -jar soapui-project-runner.jar -project path/to/soapui-project.xml 
```
