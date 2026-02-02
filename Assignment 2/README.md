# Assignment 2 - CI/CD Pipeline with GitLab

## Descrizione del Progetto
Questo repository contiene l'implementazione di una pipeline CI/CD per un'applicazione Spring Boot (spring-one-books) utilizzando GitLab CI/CD. La pipeline automatizza i processi di build, verifica, test, packaging, release e documentazione.

Il repository **spring-one-books** è un’applicazione di esempio in **Java con Spring Boot e Spring for GraphQL** che implementa una semplice API GraphQL per gestire informazioni su libri e autori. Mostra come creare uno schema GraphQL *schema-first* definendo tipi (`Book`, `Author`, input per filtri e una union `SearchItem`) e query per ottenere tutti i libri, un libro per ID, tutti gli autori e ricerche testuali. L’applicazione contiene componenti principali come *records* Java per i modelli dati, un servizio `BookService` che popola dati in memoria, e un controller `BookController` con metodi annotati (`@QueryMapping`, `@SchemaMapping`, `@BatchMapping`) per rispondere alle query GraphQL, gestire filtri e affrontare il problema N+1 con *batch loading*. L’app include inoltre esempi di gestione di query complesse e caricamento efficiente di associazioni tra autori e libri tramite *BatchMapping*.

## Membri del Gruppo
- Sanaa Msellek 902325
- Christena Attia 894887
- Arianna Silvestri 869617

## URL del Repository
Repository originale: https://github.com/danvega/spring-one-books

Repository con pipeline CI/CD creata dal Gruppo 9: https://gitlab.com/groupNine/2025_assignment2_spring-one-books.git

## Organizzazione del lavoro
Nella prima fase del lavoro, quella di impostazione della pipeline, il team si è incontrato in presenza per discutere di quali strumenti utilizzare, motivo per il quale risultano commit fatte da una sola persona direttamente sul master. Successivamente, una volta stabilito il flusso di lavoro e suddivise le responsabilità, sono stati creati dei branch individuali per le modifiche relative agli stage assegnati a ciascun membro del team e per la stesura del README.

## Descrizione delle Stage della Pipeline
### Variabili 
La pipeline utilizza una serie di variabili d'ambiente che semplificano la configurazione dei job.

1. `DOCKER_HOST`: **tcp://docker:2375**
Indica al runner Docker su quale socket comunicare. Permette la creazione e la gestione di container durante la pipeline.

2. `DOCKER_DRIVER`: **overlay2**
Serve per impostare il Docker driver.

3. `CONTAINER_IMAGE`: **$CI_REGISTRY_IMAGE**
Indica il nome e il percorso del progetto nel GitLab Container Registry.

4. `MAVEN_CLI_OPTS`: **"--strict-checksums --threads 1C --batch-mode"**
Opzione di comando per Maven per verificare l'integrità dei file scaricati, per l'ottimizzazione dell'uso della CPU e per esecuzioni automatizzate.

5. `MAVEN_OPTS`: **"-Dmaven.repo.local=.m2/repository"**
Serve per l'utilizzo di una repository locale per le dipendenze scaricate

### Cache
La pipeline utilizza una cache globale definita con percorso `.m2/repository/` e che dipende dal file `pom.xml`. Ogni cambiamento relativo a quest'ultimo verrà registrato sulla cache. Inoltre, ciascun branch possiede una propria cache indipendente rispetto a quella degli altri branch per evitare di scaricare le dipendenze ogni volta che si fa partire la pipeline. 

### 1. Build
Questo stage verifica che il progetto sia compilabile prima di eseguire controlli o test. Il job scarica le dipendenze esterne che vengono poi salvate nella cache della pipeline. 
I file compilati che vengono prodotti sono salvati come artefatti del job, in modo da poter essere riutilizzati dai job degli stage successivi.
Il comando `mvn compile` è sufficiente per il nostro scopo, senza svolgere verifiche o test e senza preparare il pacchetto in anticipo, processi a cui sono stati dedicati gli stage e i job successivi.

### 2. Verify
Questo stage implementa l'analisi del codice a livello statico e dinamico in parallelo: tutti i job di questo stage vengono eseguiti contemporaneamente.

#### Static Analysis
Per l'analisi statica del codice sono stati sviluppati due job.

Il primo job utilizza il tool `Checkstyle` lanciato con Maven e permette di analizzare il codice sorgente a livello statico, controllando lo stile e verificare che il codice sia coerente con le regole definite da `Checkstyle`.
Per non rallentare lo sviluppo, il fallimento del job viene ignorato, ma viene registrato salvando l'artefatto prodotto.
Come specificato sul sito di `Checkstyle`, per funzionare correttamente una limitazione all'uso di questo strumento richiede che il codice sia compilabile, altrimenti errori di compilazione dovuti alla sintassi vengono segnalati come errori di parsing difficili da interpretare. 
Per questo motivo nel seguente job si è preferito mantenere la dipendenza implicita dal job di compilazione.
Per il job di `Checkstyle`, il file `pom.xml` è stato integrato con il `Maven Plugin CheckStyle`, con `versione 3.3.0` (una versione stabile).

**Motivazione della scelta**: Checkstyle è stato scelto perché è uno strumento maturo e ampiamente adottato nella comunità Java per mantenere uno stile di codice uniforme. L’uso delle `allow_failure: true` permette di identificare problemi di stile senza bloccare l’integrazione continua, consentendo al team di correggere gradualmente le non conformità senza rallentare lo sviluppo.


Il secondo job utilizza la libreria `SpotBugs` lanciata con Maven e permette di analizzare i file compilati, salvati alla fine del job precedente, a livello statico, evidenziando eventuali bug o errori di configurazione nel bytecode java.
Questo job utilizza l'artefatto del job build, che viene scaricato tramite la dipendenza dal job di build.
Anche in questo caso, per non bloccare lo sviluppo, il fallimento del job viene ignorato, ma viene in ogni caso registrato salvando l'artefatto prodotto.
Per il job di Checkstyle, il file `pom.xml` è stato integrato con il `Maven Plugin Spotbugs`, con `versione 4.8.3.0` (una versione stabile).

**Motivazione della scelta**: SpotBugs rileva bug potenziali e pratiche rischiose a livello di bytecode (ad esempio null pointer, race condition, resource leak). Anche in questo caso, `allow_failure: true` permette di raccogliere segnalazioni senza bloccare il flusso. La combinazione Checkstyle + SpotBugs offre una copertura completa sia sullo stile che sulla qualità del codice.

Infatti, grazie a questo tool abbiamo identificato un'esposizione della rappresentazione interna negli accessor (i metodi `findAll` e `findAllAuthors`) che abbiamo successivamente corretto. 

Versione precedente:

    public List<Book> findAll() {
        return books;
    }

    public Book findById(String id) {
    }

    public List<Author> findAllAuthors() {
        return authors;
    }

Versione attuale: 

    public List<Book> findAll() {
        return List.of(books.toArray(new Book[0]));
    }

    public Book findById(String id) {
    }

    public List<Author> findAllAuthors() {
        return List.of(authors.toArray(new Author[0]));
    }

#### Analisi Dinamica con JFR
È stato usato Java Flight Recorder (JFR) durante lo stage di verify per eseguire un'analisi dinamica completa.
Questo strumento avvia automaticamente una sessione di profiling di 20 secondi parallelamente all'esecuzione della suite di test con `mvn verify`. JFR è nativo della JVM per cui raccoglie dati diagnostici:

- Utilizzo della CPU: Tempo di esecuzione dei metodi, hot spots
- Utilizzo della memoria: Allocazioni, garbage collection, utilizzo heap
- I/O: Operazioni di lettura/scrittura, latenze di rete
- Lock e sincronizzazione: Contenzioni, tempi di attesa
- Attività di thread: Stato, creazione, terminazione

Il file di output, con estensione `.jfr`, è stato poi aperto e analizzato utilizzando `VisualVM`.

**Motivazione della scelta**: JFR permette di profilare l’applicazione in modo non invasivo, raccogliendo metriche di runtime preziose che aiutano a individuare colli di bottiglia e anomalie prestazionali già durante la fase di integrazione continua, anziché aspettare test di performance dedicati. Nel nostro caso, il profiler lavora durante l'esecuzione dei test unitari e di integrazione per capire non solo se il codice funziona ma anche la sua performance sotto il carico operativo. 


### 3. Test
Lo stage di test esegue automaticamente l’intera suite di test del progetto, composta da test unitari e test di integrazione.

- I test unitari validano in modo isolato la logica delle singole componenti, assicurando che ciascun modulo si comporti secondo le specifiche attese.
- I test di integrazione verificano il corretto funzionamento dei flussi applicativi più ampi, garantendo che i vari componenti collaborino correttamente tra loro.

Durante la pipeline CI, entrambi i gruppi di test vengono eseguiti per prevenire regressioni ed errori funzionali. In caso di fallimento di uno qualsiasi dei test, la pipeline viene automaticamente interrotta per evitare la propagazione di codice non conforme.

- **Strumenti utilizzati**: Maven per l'esecuzione, JUnit 5 come framework di testing, e Spring Boot Test per il contesto dell'applicazione


##### Code Coverage Analysis - Jacoco

Lo stage dedicato a JaCoCo genera un report dettagliato della code coverage basato sull’esecuzione di tutti i test (unitari e di integrazione).
Il report indica quali parti del codice risultano coperte dai test e quali necessitano di ulteriore verifica. Questo permette al team di:
- monitorare l’efficacia della suite di test;
- identificare codice non testato;
- mantenere un livello qualitativo elevato del progetto.

In caso di successo della suite dei test, il report viene pubblicato come artefatto della pipeline così da poter essere consultato.

**Motivazione della scelta**: JaCoCo è uno strumento standard di Java per misurare la coverage. Integrarlo nella pipeline fornisce visibilità immediata sulle porzioni di codice scoperte, aiutando il team a mantenere test di qualità e a prendere decisioni informate sull’aggiunta di nuovi test.

### 4. Package
In questa fase viene creato il pacchetto finale dell'applicazione pronto per la distribuzione. Utilizzando il Spring Boot Maven Plugin, il processo di packaging genera un JAR autonomo ed eseguibile che contiene tutte le dipendenze necessarie. Questo artefatto può essere eseguito direttamente senza richiedere un application server esterno, semplificando notevolmente il deployment. Il JAR risultante include sia il codice compilato dell'applicazione che tutte le librerie dipendenti, rendendolo completamente portabile tra diversi ambienti.

Il processo:
- **Pulizia e compilazione**: Viene eseguita una pulizia completa del progetto seguita dalla compilazione del codice sorgente
- **Gestione dipendenze**: Tutte le librerie necessarie (Spring Boot, testing, utility) vengono scaricate automaticamente da Maven Central
- **Creazione JAR**: Viene generato un JAR standard contenente le classi compilate
- **Spring Boot Repackage**: Il plugin Spring Boot trasforma il JAR standard in un **executable fat JAR** che include:
  - Tutte le dipendenze dell'applicazione nella cartella `BOOT-INF/lib/`
  - Le classi dell'applicazione in `BOOT-INF/classes/`
  - Un loader specializzato che permette l'esecuzione autonoma

Viene prodotto il file `springbooks-0.0.1-SNAPSHOT.jar` che può essere eseguito direttamente con `java -jar` senza bisogno di application server esterni. Il JAR generato viene salvato come artifact di GitLab CI per utilizzo successivo.

**Motivazione della scelta**: Il packaging tramite Spring Boot Maven Plugin è la scelta naturale per applicazioni Spring Boot, poiché produce un JAR auto-contenuto pronto per il deployment. Saltare i test (`-DskipTests`) in questo stage è accettabile perché i test sono già stati eseguiti nello stage precedente, riducendo i tempi di esecuzione senza compromettere la qualità.

### 5. Release
La fase di Release si occupa della creazione e pubblicazione dell'immagine Docker dell'applicazione. Sfrutta il file `JAR` prodotto nello stage Package per costruire un'immagine contenitore che può essere distribuita ed eseguita in qualsiasi ambiente compatibile con Docker.
Il job usa Docker-in-Docker (DinD), in modo tale da eseguire la build in un ambiente isolato.

1. Esegue il login al Container Registry di GitLab.
2. Tenta un docker pull dell'ultima immagine `($CI_COMMIT_REF_NAME)` per sfruttare la cache di build.
3. Costruisce la nuova immagine Docker, etichettandola con il nome del branch/tag.
4. Esegue il docker push dell'immagine nel Container Registry di GitLab.

### 6. Docs
Questa fase genera la documentazione tecnica dell'applicazione utilizzando Javadoc e la pubblica come GitLab Pages per renderla facilmente accessibile via web.
Una volta completata la pipeline, il job produce un artefatto `public/` che GitLab utilizza per rendere disponibile la documentazione. In una delle ultime pipeline eseguite l'artefatto si trova all’indirizzo: `https://gitlab.com/groupNine/2025_assignment2_spring-one-books/-/jobs/12423637806/artifacts/browse/public/apidocs/`.

1. Esegue il comando `mvn javadoc:javadoc` per generare la documentazione API.
2. Sposta la documentazione generata `(target/site/apidocs)` nella cartella `public`.