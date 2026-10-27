-- ========================================================
--  LEXICORE TEXT ANALYZER - SQL DATABASE EXPORT DUMP     
--  Course: PBCST304 Object Oriented Programming (KTU S3) 
--  Generated on: 2026-10-27 22:12:37
--  Source Database: data/textanalyzer.db
--  Driver: org.sqlite.JDBC
-- ========================================================

PRAGMA foreign_keys = ON;

-- --------------------------------------------------------
-- Table structure and data for: analyses
-- --------------------------------------------------------
CREATE TABLE analyses (id TEXT PRIMARY KEY, title TEXT NOT NULL, preview TEXT, word_count INTEGER, char_count INTEGER, sentence_count INTEGER, saved_at TEXT, vowel_count INTEGER DEFAULT 0, consonant_count INTEGER DEFAULT 0, reading_ease REAL DEFAULT 0.0, longest_word TEXT DEFAULT '', reading_time_minutes REAL DEFAULT 0.0);

INSERT INTO analyses VALUES ('REC-TEST', 'Unit Test Document', 'Preview content', 50, 300, 4, '2026-10-07 21:43:11', 0, 0, 0.0, '', 0.0);
INSERT INTO analyses VALUES ('DEMO-OOP-001', 'Object-Oriented Programming (KTU S3)', 'Java incorporates encapsulation, inheritance, polymorphism, and abstraction as foundational pillars.', 85, 592, 4, '2026-10-27 10:15:00', 198, 312, 64.5, 'polymorphism', 0.4);
INSERT INTO analyses VALUES ('DEMO-OS-002', 'Operating Systems & Concurrency', 'Multithreading enables concurrent thread execution sharing heap memory but maintaining isolated call stacks.', 110, 740, 6, '2026-10-27 14:30:00', 245, 395, 58.2, 'multithreading', 0.6);
INSERT INTO analyses VALUES ('DEMO-DB-003', 'Database Persistence & JDBC Connectivity', 'The Java Database Connectivity API enables relational data storage with ACID transaction guarantees.', 94, 665, 5, '2026-10-27 18:45:00', 218, 355, 62.0, 'connectivity', 0.5);

-- --------------------------------------------------------
-- Table structure and data for: analysis_words
-- --------------------------------------------------------
CREATE TABLE analysis_words (id INTEGER PRIMARY KEY AUTOINCREMENT, analysis_id TEXT NOT NULL, word TEXT NOT NULL, frequency INTEGER NOT NULL, FOREIGN KEY(analysis_id) REFERENCES analyses(id) ON DELETE CASCADE);

INSERT INTO analysis_words VALUES (35, 'DEMO-OOP-001', 'java', 7);
INSERT INTO analysis_words VALUES (36, 'DEMO-OOP-001', 'class', 6);
INSERT INTO analysis_words VALUES (37, 'DEMO-OOP-001', 'object', 5);
INSERT INTO analysis_words VALUES (38, 'DEMO-OOP-001', 'polymorphism', 4);
INSERT INTO analysis_words VALUES (39, 'DEMO-OOP-001', 'inheritance', 4);
INSERT INTO analysis_words VALUES (40, 'DEMO-OOP-001', 'encapsulation', 3);
INSERT INTO analysis_words VALUES (41, 'DEMO-OS-002', 'thread', 8);
INSERT INTO analysis_words VALUES (42, 'DEMO-OS-002', 'process', 5);
INSERT INTO analysis_words VALUES (43, 'DEMO-OS-002', 'memory', 4);
INSERT INTO analysis_words VALUES (44, 'DEMO-OS-002', 'concurrency', 4);
INSERT INTO analysis_words VALUES (45, 'DEMO-DB-003', 'database', 6);
INSERT INTO analysis_words VALUES (46, 'DEMO-DB-003', 'jdbc', 5);
INSERT INTO analysis_words VALUES (47, 'DEMO-DB-003', 'sql', 5);
INSERT INTO analysis_words VALUES (48, 'DEMO-DB-003', 'transaction', 4);

-- --------------------------------------------------------
-- Table structure and data for: sessions
-- --------------------------------------------------------
CREATE TABLE sessions (id TEXT PRIMARY KEY, created_at TEXT NOT NULL, last_active_at TEXT NOT NULL);

INSERT INTO sessions VALUES ('c919ad76-24ee-467d-b90e-2e8f17464ca0', '2026-10-27 18:47:01', '2026-10-27 18:47:18');
INSERT INTO sessions VALUES ('verify-session-1', '2026-10-27 18:47:12', '2026-10-27 18:47:15');
INSERT INTO sessions VALUES ('a0a8d727-a897-47b5-8836-b50d08e60592', '2026-10-27 18:54:59', '2026-10-27 19:04:01');
INSERT INTO sessions VALUES ('SESS-DEMO-VIVA-01', '2026-10-27 10:00:00', '2026-10-27 18:50:00');

-- --------------------------------------------------------
-- Table structure and data for: session_activities
-- --------------------------------------------------------
CREATE TABLE session_activities (id INTEGER PRIMARY KEY AUTOINCREMENT, session_id TEXT NOT NULL, activity_type TEXT NOT NULL, source_text TEXT, result_text TEXT, extra_json TEXT, created_at TEXT NOT NULL, FOREIGN KEY(session_id) REFERENCES sessions(id));

INSERT INTO session_activities VALUES (1, 'c919ad76-24ee-467d-b90e-2e8f17464ca0', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 18:47:01');
INSERT INTO session_activities VALUES (2, 'verify-session-1', 'DICTIONARY', 'algorithm', 'A collection of ordered steps that solve a mathematical problem. A precise step-by-step plan for a computational procedure that possibly begins with an input value and yields an output value in a finite number of steps.', '{"fromCache":false}', '2026-10-27 18:47:12');
INSERT INTO session_activities VALUES (3, 'verify-session-1', 'TRANSLATE', 'Hello student', 'नमस्ते छात्र', '{"targetLang":"hi","fromCache":false}', '2026-10-27 18:47:14');
INSERT INTO session_activities VALUES (4, 'verify-session-1', 'TRANSLATE', 'Hello student', 'ഹലോ വിദ്യാർത്ഥി', '{"targetLang":"ml","fromCache":false}', '2026-10-27 18:47:15');
INSERT INTO session_activities VALUES (5, 'c919ad76-24ee-467d-b90e-2e8f17464ca0', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 18:47:18');
INSERT INTO session_activities VALUES (6, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 18:54:59');
INSERT INTO session_activities VALUES (7, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'TRANSLATE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', 'ऑब्जेक्ट - ओरिएंटेड प्रोग्रामिंग (OOP) ऑब्जेक्ट की अवधारणा के आधार पर एक मौलिक प्रोग्रामिंग प्रतिमान है। जावा में, कक्षाएं राज्य और व्यवहार को समाहित करने वाली वस्तुओं को बनाने के लिए ब्लूप्रिंट के रूप में कार्य करती हैं। OOP के मुख्य स्तंभों में encapsulation, Abstraction, Inheritance और Polymorphism शामिल हैं। इनकैप्सुलेशन निजी चर और सार्वजनिक अभिगमकर्ता विधियों के माध्यम से डेटा सुरक्षा सुनिश्चित करता है। इनहेरिटेंस एक सबक्लास को एक सुपरक्लास से फ़ील्ड और विधियों को विरासत में लेने की अनुमति देता है, जिससे उच्च कोड पुन: उपयोगिता को बढ़ावा मिलता है। बहुरूपता गतिशील विधि प्रेषण को सक्षम करती है, जहां रनटाइम पर विधि कॉल का समाधान किया जाता है। मजबूत अपवाद हैंडलिंग और ठोस डिजाइन सिद्धांतों के माध्यम से, जावा सॉफ्टवेयर इंजीनियर बड़े पैमाने पर डेटा सेट को संसाधित करने में सक्षम स्केलेबल, रखरखाव योग्य और उच्च - प्रदर्शन वाले अनुप्रयोगों का निर्माण करते हैं।', '{"targetLang":"hi","fromCache":false}', '2026-10-27 18:55:29');
INSERT INTO session_activities VALUES (8, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'DICTIONARY', 'fostering', 'To nurture or bring up offspring, or to provide similar parental care to an unrelated child.', '{"fromCache":false}', '2026-10-27 18:55:54');
INSERT INTO session_activities VALUES (9, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:01:20');
INSERT INTO session_activities VALUES (10, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'TRANSLATE', 'Hello My Friend', 'ഹലോ മൈ ഫ്രണ്ട്', '{"targetLang":"ml","fromCache":false}', '2026-10-27 19:01:59');
INSERT INTO session_activities VALUES (11, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'TRANSLATE', 'Hello My Friend', 'नमस्ते मेरे दोस्त', '{"targetLang":"hi","fromCache":false}', '2026-10-27 19:02:03');
INSERT INTO session_activities VALUES (12, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:02:16');
INSERT INTO session_activities VALUES (13, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:02:17');
INSERT INTO session_activities VALUES (14, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:02:17');
INSERT INTO session_activities VALUES (15, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:02:17');
INSERT INTO session_activities VALUES (16, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:02:17');
INSERT INTO session_activities VALUES (17, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:03:02');
INSERT INTO session_activities VALUES (18, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'DICTIONARY', 'hinder', 'To make difficult to accomplish; to act as an obstacle; to frustrate.', '{"fromCache":false}', '2026-10-27 19:03:39');
INSERT INTO session_activities VALUES (19, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'object-oriented programming (oop) is a fundamental programming paradigm based on the concept of objects. in java, classes act as blueprints for creating objects that encapsulate state and behavior. the core pillars of oop include encapsulation, abstraction, inheritance, and polymorphism.

encapsulation ensures data protection through private variables and public accessor methods. inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

through robust exception handling and solid design principles, java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:03:50');
INSERT INTO session_activities VALUES (20, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-oriented programming (oop) is a fundamental programming paradigm based on the concept of objects. In java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of oop include encapsulation, abstraction, inheritance, and polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and solid design principles, java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:03:53');
INSERT INTO session_activities VALUES (21, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'Object-Oriented Programming (Oop) Is A Fundamental Programming Paradigm Based On The Concept Of Objects. In Java, Classes Act As Blueprints For Creating Objects That Encapsulate State And Behavior. The Core Pillars Of Oop Include Encapsulation, Abstraction, Inheritance, And Polymorphism.

Encapsulation Ensures Data Protection Through Private Variables And Public Accessor Methods. Inheritance Allows A Subclass To Inherit Fields And Methods From A Superclass, Fostering High Code Reusability. Polymorphism Enables Dynamic Method Dispatch, Where Method Calls Are Resolved At Runtime.

Through Robust Exception Handling And Solid Design Principles, Java Software Engineers Construct Scalable, Maintainable, And High-Performance Applications Capable Of Processing Massive Data Sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:03:56');
INSERT INTO session_activities VALUES (22, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'OBJECT-ORIENTED PROGRAMMING (OOP) IS A FUNDAMENTAL PROGRAMMING PARADIGM BASED ON THE CONCEPT OF OBJECTS. IN JAVA, CLASSES ACT AS BLUEPRINTS FOR CREATING OBJECTS THAT ENCAPSULATE STATE AND BEHAVIOR. THE CORE PILLARS OF OOP INCLUDE ENCAPSULATION, ABSTRACTION, INHERITANCE, AND POLYMORPHISM.

ENCAPSULATION ENSURES DATA PROTECTION THROUGH PRIVATE VARIABLES AND PUBLIC ACCESSOR METHODS. INHERITANCE ALLOWS A SUBCLASS TO INHERIT FIELDS AND METHODS FROM A SUPERCLASS, FOSTERING HIGH CODE REUSABILITY. POLYMORPHISM ENABLES DYNAMIC METHOD DISPATCH, WHERE METHOD CALLS ARE RESOLVED AT RUNTIME.

THROUGH ROBUST EXCEPTION HANDLING AND SOLID DESIGN PRINCIPLES, JAVA SOFTWARE ENGINEERS CONSTRUCT SCALABLE, MAINTAINABLE, AND HIGH-PERFORMANCE APPLICATIONS CAPABLE OF PROCESSING MASSIVE DATA SETS.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:03:58');
INSERT INTO session_activities VALUES (23, 'a0a8d727-a897-47b5-8836-b50d08e60592', 'ANALYZE', 'object-oriented programming (oop) is a fundamental programming paradigm based on the concept of objects. in java, classes act as blueprints for creating objects that encapsulate state and behavior. the core pillars of oop include encapsulation, abstraction, inheritance, and polymorphism.

encapsulation ensures data protection through private variables and public accessor methods. inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

through robust exception handling and solid design principles, java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', '101 words, 7 sentences', '{"sourceName":"Welcome Sample","wordCount":101}', '2026-10-27 19:04:01');
INSERT INTO session_activities VALUES (24, 'SESS-DEMO-VIVA-01', 'ANALYZE', 'Java OOP principles sample text', '85 words, 592 chars', '{}', '2026-10-27 10:15:00');
INSERT INTO session_activities VALUES (25, 'SESS-DEMO-VIVA-01', 'SEARCH', 'polymorphism', 'Found 4 occurrences', '{"query":"polymorphism"}', '2026-10-27 10:18:00');
INSERT INTO session_activities VALUES (26, 'SESS-DEMO-VIVA-01', 'DICTIONARY', 'polymorphism', 'The condition of occurring in several different forms.', '{}', '2026-10-27 10:20:00');
INSERT INTO session_activities VALUES (27, 'SESS-DEMO-VIVA-01', 'TRANSLATE', 'Object-Oriented Programming', 'ऑब्जेक्ट-ओरिएंटेड प्रोग्रामिंग (hi)', '{"lang":"hi"}', '2026-10-27 10:25:00');
INSERT INTO session_activities VALUES (28, 'SESS-DEMO-VIVA-01', 'EXPORT', 'DEMO-OOP-001', 'Report exported to data/reports/report_DEMO-OOP-001.txt', '{}', '2026-10-27 10:30:00');
INSERT INTO session_activities VALUES (29, 'SESS-DEMO-VIVA-01', 'ANALYZE', 'Java OOP principles sample text', '85 words, 592 chars', '{}', '2026-10-27 10:15:00');
INSERT INTO session_activities VALUES (30, 'SESS-DEMO-VIVA-01', 'SEARCH', 'polymorphism', 'Found 4 occurrences', '{"query":"polymorphism"}', '2026-10-27 10:18:00');
INSERT INTO session_activities VALUES (31, 'SESS-DEMO-VIVA-01', 'DICTIONARY', 'polymorphism', 'The condition of occurring in several different forms.', '{}', '2026-10-27 10:20:00');
INSERT INTO session_activities VALUES (32, 'SESS-DEMO-VIVA-01', 'TRANSLATE', 'Object-Oriented Programming', 'ऑब्जेक्ट-ओरिएंटेड प्रोग्रामिंग (hi)', '{"lang":"hi"}', '2026-10-27 10:25:00');
INSERT INTO session_activities VALUES (33, 'SESS-DEMO-VIVA-01', 'EXPORT', 'DEMO-OOP-001', 'Report exported to data/reports/report_DEMO-OOP-001.txt', '{}', '2026-10-27 10:30:00');
INSERT INTO session_activities VALUES (34, 'SESS-DEMO-VIVA-01', 'ANALYZE', 'Java OOP principles sample text', '85 words, 592 chars', '{}', '2026-10-27 10:15:00');
INSERT INTO session_activities VALUES (35, 'SESS-DEMO-VIVA-01', 'SEARCH', 'polymorphism', 'Found 4 occurrences', '{"query":"polymorphism"}', '2026-10-27 10:18:00');
INSERT INTO session_activities VALUES (36, 'SESS-DEMO-VIVA-01', 'DICTIONARY', 'polymorphism', 'The condition of occurring in several different forms.', '{}', '2026-10-27 10:20:00');
INSERT INTO session_activities VALUES (37, 'SESS-DEMO-VIVA-01', 'TRANSLATE', 'Object-Oriented Programming', 'ऑब्जेक्ट-ओरिएंटेड प्रोग्रामिंग (hi)', '{"lang":"hi"}', '2026-10-27 10:25:00');
INSERT INTO session_activities VALUES (38, 'SESS-DEMO-VIVA-01', 'EXPORT', 'DEMO-OOP-001', 'Report exported to data/reports/report_DEMO-OOP-001.txt', '{}', '2026-10-27 10:30:00');

-- --------------------------------------------------------
-- Table structure and data for: dictionary_cache
-- --------------------------------------------------------
CREATE TABLE dictionary_cache (word TEXT PRIMARY KEY, phonetic TEXT, definition_json TEXT, cached_at TEXT);

INSERT INTO dictionary_cache VALUES ('algorithm', '/ˈælɡəɹɪðm̩/', '{"word":"algorithm","phonetic":"/ˈælɡəɹɪðm̩/","audioUrl":"https://api.dictionaryapi.dev/media/pronunciations/en/algorithm-us.mp3","fromCache":false,"meanings":[{"partOfSpeech":"noun","definitions":[{"definition":"A collection of ordered steps that solve a mathematical problem. A precise step-by-step plan for a computational procedure that possibly begins with an input value and yields an output value in a finite number of steps.","example":""}],"synonyms":[]}]}', '2026-10-27 18:47:12');
INSERT INTO dictionary_cache VALUES ('fostering', '', '{"word":"fostering","phonetic":"","audioUrl":"","fromCache":false,"meanings":[{"partOfSpeech":"verb","definitions":[{"definition":"To nurture or bring up offspring, or to provide similar parental care to an unrelated child.","example":""}],"synonyms":[]},{"partOfSpeech":"noun","definitions":[{"definition":"Raising someone to be an accepted member of the community.","example":""}],"synonyms":[]}]}', '2026-10-27 18:55:53');
INSERT INTO dictionary_cache VALUES ('hinder', '/ˈhaɪn.də/', '{"word":"hinder","phonetic":"/ˈhaɪn.də/","audioUrl":"https://api.dictionaryapi.dev/media/pronunciations/en/hinder-1-au.mp3","fromCache":false,"meanings":[{"partOfSpeech":"verb","definitions":[{"definition":"To make difficult to accomplish; to act as an obstacle; to frustrate.","example":"A drought hinders the growth of plants."},{"definition":"To delay or impede; to keep back, to prevent.","example":"She hindered a man from committing suicide."},{"definition":"To cause harm.","example":""}],"synonyms":[]},{"partOfSpeech":"adjective","definitions":[{"definition":"Located at the rear (most often said of animals'' body parts).","example":""}],"synonyms":[]},{"partOfSpeech":"noun","definitions":[{"definition":"The buttocks.","example":""}],"synonyms":[]},{"partOfSpeech":"adjective","definitions":[{"definition":"Of or belonging to that part or end which is in the rear or hind, or which follows.","example":"the hinder end of a wagon"}],"synonyms":[]}]}', '2026-10-27 19:03:39');
INSERT INTO dictionary_cache VALUES ('polymorphism', '/ˌpɒlɪˈmɔːfɪz(ə)m/', '{"partOfSpeech":"noun","definition":"The condition of occurring in several different forms in programming."}', '2026-10-27 10:20:00');
INSERT INTO dictionary_cache VALUES ('encapsulation', '/ɪnˌkæpsjʊˈleɪʃn/', '{"partOfSpeech":"noun","definition":"The bundling of data with the methods that operate on that data."}', '2026-10-27 10:22:00');

-- --------------------------------------------------------
-- Table structure and data for: translation_cache
-- --------------------------------------------------------
CREATE TABLE translation_cache (source_hash TEXT NOT NULL, source_text TEXT, target_lang TEXT NOT NULL, translated_text TEXT, created_at TEXT, PRIMARY KEY (source_hash, target_lang));

INSERT INTO translation_cache VALUES ('3649291c41db64f141925191e70166e8962722814adf3ec3ac02ceaf61fc7a73', 'Hello student', 'hi', 'नमस्ते छात्र', '2026-10-27 18:47:14');
INSERT INTO translation_cache VALUES ('fdc309429165d5f7120122af02592b9224ef1923dd147159ab34d41923aad601', 'Hello student', 'ml', 'ഹലോ വിദ്യാർത്ഥി', '2026-10-27 18:47:15');
INSERT INTO translation_cache VALUES ('4ae4f2ccbfd6eec410a3d5d6bc44962bc728b7942f19cb854e53e4b7a18b7fb4', 'Object-Oriented Programming (OOP) is a fundamental programming paradigm based on the concept of objects. In Java, classes act as blueprints for creating objects that encapsulate state and behavior. The core pillars of OOP include Encapsulation, Abstraction, Inheritance, and Polymorphism.

Encapsulation ensures data protection through private variables and public accessor methods. Inheritance allows a subclass to inherit fields and methods from a superclass, fostering high code reusability. Polymorphism enables dynamic method dispatch, where method calls are resolved at runtime.

Through robust exception handling and SOLID design principles, Java software engineers construct scalable, maintainable, and high-performance applications capable of processing massive data sets.', 'hi', 'ऑब्जेक्ट - ओरिएंटेड प्रोग्रामिंग (OOP) ऑब्जेक्ट की अवधारणा के आधार पर एक मौलिक प्रोग्रामिंग प्रतिमान है। जावा में, कक्षाएं राज्य और व्यवहार को समाहित करने वाली वस्तुओं को बनाने के लिए ब्लूप्रिंट के रूप में कार्य करती हैं। OOP के मुख्य स्तंभों में encapsulation, Abstraction, Inheritance और Polymorphism शामिल हैं। इनकैप्सुलेशन निजी चर और सार्वजनिक अभिगमकर्ता विधियों के माध्यम से डेटा सुरक्षा सुनिश्चित करता है। इनहेरिटेंस एक सबक्लास को एक सुपरक्लास से फ़ील्ड और विधियों को विरासत में लेने की अनुमति देता है, जिससे उच्च कोड पुन: उपयोगिता को बढ़ावा मिलता है। बहुरूपता गतिशील विधि प्रेषण को सक्षम करती है, जहां रनटाइम पर विधि कॉल का समाधान किया जाता है। मजबूत अपवाद हैंडलिंग और ठोस डिजाइन सिद्धांतों के माध्यम से, जावा सॉफ्टवेयर इंजीनियर बड़े पैमाने पर डेटा सेट को संसाधित करने में सक्षम स्केलेबल, रखरखाव योग्य और उच्च - प्रदर्शन वाले अनुप्रयोगों का निर्माण करते हैं।', '2026-10-27 18:55:29');
INSERT INTO translation_cache VALUES ('6de209e4875676bee4f1de7aeb887ad3e7a5284c252bcb02f32720884fd861bb', 'Hello My Friend', 'ml', 'ഹലോ മൈ ഫ്രണ്ട്', '2026-10-27 19:01:59');
INSERT INTO translation_cache VALUES ('f2f599cde89602c4dd24e7754b8bd2db08ac2791afbbde7276b72daac1351d80', 'Hello My Friend', 'hi', 'नमस्ते मेरे दोस्त', '2026-10-27 19:02:03');
INSERT INTO translation_cache VALUES ('hash_oop_en_hi', 'Object-Oriented Programming', 'hi', 'ऑब्जेक्ट-ओरिएंटेड प्रोग्रामिंग', '2026-10-27 10:25:00');
INSERT INTO translation_cache VALUES ('hash_oop_en_ml', 'Object-Oriented Programming', 'ml', 'ഒബ്ജക്റ്റ്-ഓറിയന്റഡ് പ്രോഗ്രാമിംഗ്', '2026-10-27 10:26:00');

