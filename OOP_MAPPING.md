# Object-Oriented Programming (OOP) Syllabus & Specification Mapping

**Course Code:** PBCST304 — Object Oriented Programming (KTU S3 Syllabus)  
**Project:** Word Counter & Text Analyzer Web Application  
**Backend:** Java 21 LTS (com.sun.net.httpserver, Pure OOP, Zero External Dependencies)  
**Frontend:** Responsive HTML5 / CSS3 / ES6 JavaScript Web App  
**Bridging Architecture:** Model-View-Controller (MVC) with RESTful JSON API  

---

## 1. Compliance with PBCST304 OOP Syllabus

### Module 1: Introduction to Java & OOP Fundamentals
| Syllabus Topic | Implementation in Project | Source Files |
| :--- | :--- | :--- |
| **Java Program Structure & Environment** | Clean package layout, standard main entry point, compilation with JDK 21 | [`Main.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/Main.java) |
| **Primitive Data Types & Wrappers** | `int`, `long`, `char`, `boolean` alongside `Integer`, `Double`, `Boolean` | [`AnalysisResult.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/model/AnalysisResult.java), [`TextUtils.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/util/TextUtils.java) |
| **Autoboxing & Unboxing** | Automatic boxing between `int` and `Integer` in Collections frequency maps | [`ComprehensiveTextAnalyzer.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core/ComprehensiveTextAnalyzer.java) |
| **Arrays & Strings** | Character arrays (`char[]`), string tokenization, regex splitting | [`TextUtils.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/util/TextUtils.java) |
| **String & StringBuilder Classes** | Extensively used for mutable string manipulation and report generation | [`TextReportExporter.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/pattern/TextReportExporter.java) |
| **Control Statements** | Selection (`if-else`, `switch`), Iteration (`for-each`, `while`), Jump (`break`, `continue`) | [`TextAnalyzerConsoleApp.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/cli/TextAnalyzerConsoleApp.java) |
| **Classes, Abstract Classes & Interfaces** | `AbstractTextAnalyzer`, `TextAnalyzer`, `Searchable`, `Transformable` | [`com.textanalyzer.core`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core) |
| **Naming Conventions** | Strict PascalCase for classes, camelCase for methods/variables, UPPER_CASE for constants | Across all classes |

---

### Module 2: Object-Oriented Programming & Polymorphism
| Syllabus Topic | Implementation in Project | Source Files |
| :--- | :--- | :--- |
| **Declaring Objects & References** | Object instantiations across model, service, and controller layers | Throughout codebase |
| **Constructors & `this` Keyword** | Constructor chaining via `this(...)` and disambiguating instance fields | [`TextDocument.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/model/TextDocument.java), [`AbstractTextAnalyzer.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core/AbstractTextAnalyzer.java) |
| **Access Modifiers** | `private` encapsulation, `protected` base class fields, `public` API | [`AbstractTextAnalyzer.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core/AbstractTextAnalyzer.java) |
| **Method Overloading (Compile-time Polymorphism)** | Overloaded `analyze()`, `search()`, and constructor overloads | [`ComprehensiveTextAnalyzer.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core/ComprehensiveTextAnalyzer.java) |
| **Method Overriding & Dynamic Method Dispatch** | Overriding abstract methods and interface contracts resolved at runtime | [`ComprehensiveTextAnalyzer.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core/ComprehensiveTextAnalyzer.java) |
| **Objects as Parameters & Returning Objects** | Passing `TextDocument` and returning `AnalysisResult`, `SearchResult` | Core analyzer methods |
| **Static Members** | Static utility methods in `TextUtils`, static singleton instance | [`TextUtils.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/util/TextUtils.java), [`StorageManager.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/pattern/StorageManager.java) |
| **Final Variables** | Immutable model fields (`final String content`), constants | [`TextDocument.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/model/TextDocument.java) |
| **Inner Classes** | Static inner `Builder` class inside `AnalysisResult` | [`AnalysisResult.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/model/AnalysisResult.java#L106-L150) |
| **Inheritance & `super` Keyword** | `ComprehensiveTextAnalyzer extends AbstractTextAnalyzer` calling `super(...)` | [`ComprehensiveTextAnalyzer.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core/ComprehensiveTextAnalyzer.java#L30-L38) |

---

### Module 3: Packages, Interfaces, Exceptions & Design Patterns
| Syllabus Topic | Implementation in Project | Source Files |
| :--- | :--- | :--- |
| **Packages & Access Protection** | Modular packages: `model`, `core`, `pattern`, `storage`, `exception`, `server`, `cli`, `util` | Directory structure under `src/com/textanalyzer/` |
| **Interfaces vs Abstract Classes** | Pure interfaces (`TextAnalyzer`, `Searchable`) vs abstract base (`AbstractTextAnalyzer`) | [`TextAnalyzer.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core/TextAnalyzer.java), [`AbstractTextAnalyzer.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core/AbstractTextAnalyzer.java) |
| **Custom Checked Exceptions** | `TextAnalysisException` and `StorageException` extending `Exception` with `throw` / `throws` | [`TextAnalysisException.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/exception/TextAnalysisException.java), [`StorageException.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/exception/StorageException.java) |
| **Unchecked Exceptions** | `InvalidInputException` extending `RuntimeException` | [`InvalidInputException.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/exception/InvalidInputException.java) |
| **Exception Handling Keywords** | `try`, `catch`, `finally`, `throw`, `throws`, and `try-with-resources` | [`FileSourceAdapter.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/pattern/FileSourceAdapter.java), [`FileStorageService.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/storage/FileStorageService.java) |
| **Singleton Design Pattern** | `StorageManager` with private constructor and synchronized `getInstance()` | [`StorageManager.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/pattern/StorageManager.java) |
| **Adapter Design Pattern** | `FileSourceAdapter` adapting `File` / I/O streams to `DocumentSource` interface | [`FileSourceAdapter.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/pattern/FileSourceAdapter.java) |
| **Strategy & Factory Patterns** | `ReportExporter` strategy interface, `TextReportExporter`, `JsonReportExporter`, `ReportExporterFactory` | [`com.textanalyzer.pattern`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/pattern) |

---

### Module 4: SOLID Principles, MVC Architecture & Data Persistence
| Syllabus Topic | Implementation in Project | Source Files |
| :--- | :--- | :--- |
| **Single Responsibility Principle (SRP)** | Segregated duties: `AnalysisResult` (data), `TextUtils` (algorithms), `FileStorageService` (I/O) | Each specialized class |
| **Open/Closed Principle (OCP)** | Open for extension via `TextAnalyzer` and `ReportExporter` without modifying existing core | Core and Pattern packages |
| **Liskov Substitution Principle (LSP)** | `ComprehensiveTextAnalyzer` fully substitutes `AbstractTextAnalyzer` and `TextAnalyzer` | [`ComprehensiveTextAnalyzer.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/core/ComprehensiveTextAnalyzer.java) |
| **Interface Segregation Principle (ISP)** | Granular interfaces: `TextAnalyzer`, `Searchable`, `Transformable`, `DocumentSource` | Core interfaces |
| **Dependency Inversion Principle (DIP)** | Server handlers depend on interfaces (`TextAnalyzer`, `StorageService`), not concrete classes | [`com.textanalyzer.server.handlers`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/server/handlers) |
| **Model-View-Controller (MVC)** | **Model:** Domain objects (`AnalysisResult`, `TextDocument`)<br>**View:** Modern Web UI (`index.html`, `style.css`, `app.js`)<br>**Controller:** Java HTTP Server & API Handlers dispatching user actions | Models, Web folder, Server handlers |
| **Data Persistence** | Permanent storage in `data/` directory saving analysis history and report files | [`FileStorageService.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/storage/FileStorageService.java) |

---

## 2. Compliance with Report Specification (Word_Counter_and_Text_Analyzer.pdf)

| Specification Requirement | PDF Section | Status | Implementation Details |
| :--- | :--- | :--- | :--- |
| **Menu-Driven Application** | Sec 1.2 | Passed | Provided via [`TextAnalyzerConsoleApp.java`](file:///Users/johnszachariah/Documents/Text%20Analyzer/src/com/textanalyzer/cli/TextAnalyzerConsoleApp.java) (Option 1 to 10 interactive console) |
| **Text Input (Direct & File)** | Sec 2.1.1 | Passed | Direct input in UI / Console, file upload, drag-and-drop, and file loading |
| **Word Count** | Sec 2.1.2 | Passed | Extracted via regex word tokenization, whitespace separation |
| **Character Count** | Sec 2.1.3 | Passed | Calculated with spaces, without spaces, letter count, digit count, special characters |
| **Sentence Count** | Sec 2.1.4 | Passed | Analyzes sentence-ending punctuation (`.`, `?`, `!`) |
| **Vowel & Consonant Count** | Sec 2.1.5 | Passed | Character-by-character parsing; computes total vowels, consonants, and A/E/I/O/U distribution |
| **Longest & Shortest Word** | Sec 2.1.6 | Passed | Compares lengths, displays max/min lengths and all words matching extreme lengths |
| **Word Frequency Analysis** | Sec 2.1.7 | Passed | Computed using `HashMap<String, Integer>`, sorted by count descending and rendered in chart & table |
| **Search Word** | Sec 2.1.8 | Passed | Checks presence, counts total occurrences, supports case-sensitivity, whole-word matching, snippet extraction |
| **Find & Replace** | Sec 2.1.9 | Passed | Case-sensitive and case-insensitive replacements, returns updated text |
| **Case Conversion** | Sec 2.1.10 | Passed | UPPERCASE, lowercase, Title Case, and Sentence case transformations |
| **File Analysis & Storage** | Sec 2.1.11 | Passed | File loading via `FileSourceAdapter`, persistent history in `data/history.json`, exported report files |
| **Future Enhancements** | Sec 1.3 | Passed | Visual bar charts of frequencies, Flesch Reading Ease score, and web GUI included! |
