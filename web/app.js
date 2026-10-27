/**
 * Word Counter & Text Analyzer - Frontend Application Controller
 * Handles user interactions, REST API communication with Java backend,
 * reactive state updates, and visualization rendering.
 */

document.addEventListener('DOMContentLoaded', () => {
    // State
    let lastAnalysisResult = null;
    let currentReport = null;
    let lastTranslation = '';
    let selectedFontId = localStorage.getItem('lexicoreFontId') || 'editorial';
    let historyById = {};

    function getSessionId() {
        let id = localStorage.getItem('lexicoreSessionId');
        if (!id) {
            id = (window.crypto && crypto.randomUUID)
                ? crypto.randomUUID()
                : ('SESS-' + Date.now() + '-' + Math.random().toString(16).slice(2));
            localStorage.setItem('lexicoreSessionId', id);
        }
        return id;
    }

    // Elements
    const mainTextInput = document.getElementById('mainTextInput');
    const sourceBadge = document.getElementById('sourceBadge');
    const sourceDetails = document.getElementById('sourceDetails');
    const liveWordCount = document.getElementById('liveWordCount');
    const liveCharCount = document.getElementById('liveCharCount');
    const liveCharNoSpaces = document.getElementById('liveCharNoSpaces');
    const fileInput = document.getElementById('fileInput');
    const uploadFileBtn = document.getElementById('uploadFileBtn');
    const dropOverlay = document.getElementById('dropOverlay');
    const analyzeBtn = document.getElementById('analyzeBtn');

    const clearTextBtn = document.getElementById('clearTextBtn');
    const fontCatalog = document.getElementById('fontCatalog');
    const applyFontBtn = document.getElementById('applyFontBtn');
    const fontStatusText = document.getElementById('fontStatusText');
    const dynamicFontStylesheet = document.getElementById('dynamicFontStylesheet');
    const clearSessionHistoryBtn = document.getElementById('clearSessionHistoryBtn');

    // KPI Metrics
    const metricWords = document.getElementById('metricWords');
    const metricUniqueWords = document.getElementById('metricUniqueWords');
    const metricCharsWithSpaces = document.getElementById('metricCharsWithSpaces');
    const metricCharsNoSpaces = document.getElementById('metricCharsNoSpaces');
    const metricSentences = document.getElementById('metricSentences');
    const metricParagraphs = document.getElementById('metricParagraphs');
    const metricVowelsConsonants = document.getElementById('metricVowelsConsonants');
    const metricVowelRatio = document.getElementById('metricVowelRatio');
    const metricReadingTime = document.getElementById('metricReadingTime');
    const metricAvgWordLength = document.getElementById('metricAvgWordLength');
    const metricReadabilityScore = document.getElementById('metricReadabilityScore');
    const metricReadabilityLevel = document.getElementById('metricReadabilityLevel');

    // Tabs
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabPanels = document.querySelectorAll('.tab-panel');

    // Tab 1: Frequency
    const frequencySearchInput = document.getElementById('frequencySearchInput');
    const frequencyBarsChart = document.getElementById('frequencyBarsChart');
    const frequencyTableBody = document.getElementById('frequencyTableBody');
    const frequencyCountSummary = document.getElementById('frequencyCountSummary');

    // Tab 2: Vowels & Breakdown
    const vowelA = document.getElementById('vowelA');
    const vowelE = document.getElementById('vowelE');
    const vowelI = document.getElementById('vowelI');
    const vowelO = document.getElementById('vowelO');
    const vowelU = document.getElementById('vowelU');
    const vowelCountA = document.getElementById('vowelCountA');
    const vowelCountE = document.getElementById('vowelCountE');
    const vowelCountI = document.getElementById('vowelCountI');
    const vowelCountO = document.getElementById('vowelCountO');
    const vowelCountU = document.getElementById('vowelCountU');
    const charLetters = document.getElementById('charLetters');
    const charDigits = document.getElementById('charDigits');
    const charSpecial = document.getElementById('charSpecial');
    const charTotalVowels = document.getElementById('charTotalVowels');
    const charTotalConsonants = document.getElementById('charTotalConsonants');

    // Tab 3: Extremes
    const longestLength = document.getElementById('longestLength');
    const longestWordList = document.getElementById('longestWordList');
    const shortestLength = document.getElementById('shortestLength');
    const shortestWordList = document.getElementById('shortestWordList');

    // Tab 4: Search
    const searchWordInput = document.getElementById('searchWordInput');
    const searchMatchCase = document.getElementById('searchMatchCase');
    const searchWholeWord = document.getElementById('searchWholeWord');
    const executeSearchBtn = document.getElementById('executeSearchBtn');
    const searchStatusText = document.getElementById('searchStatusText');
    const searchSnippetsContainer = document.getElementById('searchSnippetsContainer');
    const searchSnippetsList = document.getElementById('searchSnippetsList');
    const openSearchTabBtn = document.getElementById('openSearchTabBtn');

    // Tab 5: Find & Replace
    const openReplaceTabBtn = document.getElementById('openReplaceTabBtn');
    const openDictionaryTabBtn = document.getElementById('openDictionaryTabBtn');
    const openTranslateTabBtn = document.getElementById('openTranslateTabBtn');
    const dictionaryWordInput = document.getElementById('dictionaryWordInput');
    const lookupWordBtn = document.getElementById('lookupWordBtn');
    const dictionaryResultBox = document.getElementById('dictionaryResultBox');
    const translateTextBtn = document.getElementById('translateTextBtn');
    const applyTranslationBtn = document.getElementById('applyTranslationBtn');
    const translationOutput = document.getElementById('translationOutput');
    const translationMeta = document.getElementById('translationMeta');
    const sessionActivityList = document.getElementById('sessionActivityList');
    const databaseStatusText = document.getElementById('databaseStatusText');
    const findTargetInput = document.getElementById('findTargetInput');
    const findReplacementInput = document.getElementById('findReplacementInput');
    const replaceMatchCase = document.getElementById('replaceMatchCase');
    const executeReplaceBtn = document.getElementById('executeReplaceBtn');
    const applyReplaceToMainBtn = document.getElementById('applyReplaceToMainBtn');
    const replacePreviewText = document.getElementById('replacePreviewText');

    // Case Conversion Dropdown
    const caseDropdownBtn = document.getElementById('caseDropdownBtn');
    const caseDropdownMenu = document.getElementById('caseDropdownMenu');

    // Export & History
    const saveHistoryBtn = document.getElementById('saveHistoryBtn');
    const exportDropdownBtn = document.getElementById('exportDropdownBtn');
    const exportDropdownMenu = document.getElementById('exportDropdownMenu');
    const exportTxtBtn = document.getElementById('exportTxtBtn');
    const exportJsonBtn = document.getElementById('exportJsonBtn');
    const refreshHistoryBtn = document.getElementById('refreshHistoryBtn');
    const historyListContainer = document.getElementById('historyListContainer');

    // Modal & Toast
    const exportModal = document.getElementById('exportModal');
    const exportModalTitle = document.getElementById('exportModalTitle');
    const exportModalContent = document.getElementById('exportModalContent');
    const closeExportModalBtn = document.getElementById('closeExportModalBtn');
    const copyReportBtn = document.getElementById('copyReportBtn');
    const downloadReportFileBtn = document.getElementById('downloadReportFileBtn');
    const toast = document.getElementById('toast');

    // Theme Toggle
    const themeToggleBtn = document.getElementById('themeToggleBtn');

    // Initialize Theme
    const savedTheme = localStorage.getItem('theme') || 'dark';
    if (savedTheme === 'light') {
        document.body.classList.remove('dark-theme');
        document.body.classList.add('light-theme');
    }

    themeToggleBtn.addEventListener('click', () => {
        if (document.body.classList.contains('light-theme')) {
            document.body.classList.remove('light-theme');
            document.body.classList.add('dark-theme');
            localStorage.setItem('theme', 'dark');
        } else {
            document.body.classList.remove('dark-theme');
            document.body.classList.add('light-theme');
            localStorage.setItem('theme', 'light');
        }
    });

    // Toast Notification Helper
    function showToast(message) {
        toast.textContent = message;
        toast.classList.remove('hidden');
        setTimeout(() => {
            toast.classList.add('hidden');
        }, 3200);
    }

    // Live textarea keystroke statistics
    function updateLiveStats() {
        const text = mainTextInput.value;
        const charsWithSpaces = text.length;
        const charsNoSpaces = text.replace(/\s/g, '').length;
        
        // Simple client-side word count for instant response
        const words = text.trim() ? text.trim().split(/\s+/).length : 0;

        liveWordCount.textContent = words.toLocaleString();
        liveCharCount.textContent = charsWithSpaces.toLocaleString();
        liveCharNoSpaces.textContent = charsNoSpaces.toLocaleString();
    }

    mainTextInput.addEventListener('input', updateLiveStats);

    clearTextBtn.addEventListener('click', () => {
        mainTextInput.value = '';
        sourceBadge.textContent = "Manuscript";
        sourceDetails.textContent = "Compose or paste a document to analyse";
        updateLiveStats();
        resetResults();
    });

    // File Upload Handler
    uploadFileBtn.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (!file) return;
        loadFile(file);
    });

    // Drag and Drop
    window.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropOverlay.classList.remove('hidden');
    });

    dropOverlay.addEventListener('dragleave', () => {
        dropOverlay.classList.add('hidden');
    });

    dropOverlay.addEventListener('drop', (e) => {
        e.preventDefault();
        dropOverlay.classList.add('hidden');
        const file = e.dataTransfer.files[0];
        if (file) {
            loadFile(file);
        }
    });

    function loadFile(file) {
        const reader = new FileReader();
        reader.onload = (evt) => {
            const content = evt.target.result;
            mainTextInput.value = content;
            sourceBadge.textContent = `File: ${file.name}`;
            sourceDetails.textContent = `${(file.size / 1024).toFixed(1)} KB loaded`;
            updateLiveStats();
            triggerAnalysis(file.name);
            showToast(`Loaded ${file.name}`);
        };
        reader.readAsText(file);
    }

    // Tabs switching
    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabId = btn.getAttribute('data-tab');
            activateTab(tabId);
        });
    });

    function activateTab(tabId, shouldScroll = false) {
        tabBtns.forEach(b => b.classList.toggle('active', b.getAttribute('data-tab') === tabId));
        tabPanels.forEach(p => p.classList.toggle('active', p.id === tabId));

        if (tabId === 'tabHistory') {
            loadHistoryList();
            loadSessionActivities();
        }
        if (tabId === 'tabDictionary') {
            if (dictionaryWordInput) dictionaryWordInput.focus();
        }
        if (tabId === 'tabTranslate') {
            if (translateTextBtn) translateTextBtn.focus();
        }
        if (shouldScroll) {
            const targetPanel = document.getElementById(tabId);
            if (targetPanel) {
                targetPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        }
    }

    openSearchTabBtn.addEventListener('click', () => {
        activateTab('tabSearch', true);
        searchWordInput.focus();
    });

    openReplaceTabBtn.addEventListener('click', () => {
        activateTab('tabReplace', true);
        findTargetInput.focus();
    });

    if (openDictionaryTabBtn) {
        openDictionaryTabBtn.addEventListener('click', () => {
            activateTab('tabDictionary', true);
            dictionaryWordInput.focus();
        });
    }

    if (openTranslateTabBtn) {
        openTranslateTabBtn.addEventListener('click', () => {
            activateTab('tabTranslate', true);
            if (translateTextBtn) translateTextBtn.focus();
        });
    }

    // Dropdown toggles
    caseDropdownBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        caseDropdownMenu.classList.toggle('hidden');
        exportDropdownMenu.classList.add('hidden');
    });

    exportDropdownBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        exportDropdownMenu.classList.toggle('hidden');
        caseDropdownMenu.classList.add('hidden');
    });

    document.addEventListener('click', () => {
        caseDropdownMenu.classList.add('hidden');
        exportDropdownMenu.classList.add('hidden');
    });

    // =========================================================================
    // REST API Calls to Java Backend ("Something that joins them")
    // =========================================================================

    // 1. Analyze Text
    analyzeBtn.addEventListener('click', () => triggerAnalysis());

    async function triggerAnalysis(customSourceName = null) {
        const text = mainTextInput.value;
        if (!text.trim()) {
            showToast('Please enter or upload some text to analyze.');
            return;
        }

        analyzeBtn.disabled = true;
        analyzeBtn.innerHTML = 'Analyzing...';

        try {
            const response = await fetch('/api/analyze', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    text: text,
                    sourceName: customSourceName || sourceBadge.textContent,
                    sessionId: getSessionId()
                })
            });

            const res = await response.json();
            if (res.success && res.data) {
                lastAnalysisResult = res.data;
                renderAnalysisResults(res.data);
                showToast('Text analysis complete!');
            } else {
                showToast('Analysis error: ' + (res.message || 'Unknown'));
            }
        } catch (error) {
            console.error(error);
            showToast('Failed to connect to Java backend. Is the server running?');
        } finally {
            analyzeBtn.disabled = false;
            analyzeBtn.innerHTML = `
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
                </svg>
                Analyze Text
            `;
        }
    }

    // Render results from Java backend
    function renderAnalysisResults(data) {
        // KPI Metrics
        metricWords.textContent = data.wordCount.toLocaleString();
        metricUniqueWords.textContent = `${data.uniqueWordCount.toLocaleString()} unique words`;

        metricCharsWithSpaces.textContent = data.charCountWithSpaces.toLocaleString();
        metricCharsNoSpaces.textContent = `${data.charCountWithoutSpaces.toLocaleString()} without spaces`;

        metricSentences.textContent = data.sentenceCount.toLocaleString();
        metricParagraphs.textContent = `${data.paragraphCount.toLocaleString()} paragraphs`;

        metricVowelsConsonants.textContent = `${data.totalVowels} / ${data.totalConsonants}`;
        const totalVowelConsonant = data.totalVowels + data.totalConsonants;
        const vPct = totalVowelConsonant > 0 ? ((data.totalVowels / totalVowelConsonant) * 100).toFixed(0) : 0;
        metricVowelRatio.textContent = `${vPct}% Vowels : ${100 - vPct}% Consonants`;

        metricReadingTime.textContent = `${data.readingTimeMinutes.toFixed(1)}m`;
        metricAvgWordLength.textContent = `Avg: ${data.averageWordLength.toFixed(1)} chars/word`;

        metricReadabilityScore.textContent = data.fleschReadingEase.toFixed(1);
        metricReadabilityLevel.textContent = data.readabilityLevel;

        // Render Tab 1: Frequencies
        renderFrequencies(data.wordFrequencies);

        // Render Tab 2: Vowels & Breakdown
        renderVowels(data.vowelBreakdown, data.totalVowels);
        charLetters.textContent = data.letterCount.toLocaleString();
        charDigits.textContent = data.digitCount.toLocaleString();
        charSpecial.textContent = data.specialCharCount.toLocaleString();
        charTotalVowels.textContent = data.totalVowels.toLocaleString();
        charTotalConsonants.textContent = data.totalConsonants.toLocaleString();

        // Render Tab 3: Extremes
        longestLength.textContent = data.longestWordLength;
        longestWordList.innerHTML = (data.longestWords && data.longestWords.length > 0)
            ? data.longestWords.map(w => `<span class="word-tag">${escapeHtml(w)}</span>`).join('')
            : '<span class="empty-badge">None</span>';

        shortestLength.textContent = data.shortestWordLength;
        shortestWordList.innerHTML = (data.shortestWords && data.shortestWords.length > 0)
            ? data.shortestWords.map(w => `<span class="word-tag">${escapeHtml(w)}</span>`).join('')
            : '<span class="empty-badge">None</span>';
    }

    function renderFrequencies(frequencies) {
        if (!frequencies || frequencies.length === 0) {
            frequencyBarsChart.innerHTML = '<div class="empty-state">No words found in text.</div>';
            frequencyTableBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No data</td></tr>';
            frequencyCountSummary.textContent = '0 words';
            return;
        }

        const maxCount = frequencies[0].count || 1;
        const topWords = frequencies.slice(0, 15);

        // Chart Bars
        frequencyBarsChart.innerHTML = topWords.map(item => {
            const widthPct = ((item.count / maxCount) * 100).toFixed(1);
            return `
                <div class="bar-row">
                    <span class="bar-word" title="${escapeHtml(item.word)}">${escapeHtml(item.word)}</span>
                    <div class="bar-track">
                        <div class="bar-fill" style="width: ${widthPct}%"></div>
                    </div>
                    <span class="bar-count">${item.count}</span>
                </div>
            `;
        }).join('');

        // Table
        renderFrequencyTable(frequencies);
    }

    function renderFrequencyTable(frequencies, filter = '') {
        const filtered = filter
            ? frequencies.filter(f => f.word.toLowerCase().includes(filter.toLowerCase()))
            : frequencies;

        frequencyCountSummary.textContent = `Displaying ${filtered.length} of ${frequencies.length} unique words`;

        if (filtered.length === 0) {
            frequencyTableBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No matching words</td></tr>';
            return;
        }

        frequencyTableBody.innerHTML = filtered.slice(0, 100).map((f, idx) => `
            <tr>
                <td>${idx + 1}</td>
                <td><strong class="clickable-word" data-word="${escapeHtml(f.word)}">${escapeHtml(f.word)}</strong></td>
                <td>${f.count}</td>
                <td>${f.percentage}%</td>
            </tr>
        `).join('');
    }

    frequencySearchInput.addEventListener('input', (e) => {
        if (lastAnalysisResult && lastAnalysisResult.wordFrequencies) {
            renderFrequencyTable(lastAnalysisResult.wordFrequencies, e.target.value.trim());
        }
    });

    function renderVowels(vowelMap, totalVowels) {
        const vowels = ['a', 'e', 'i', 'o', 'u'];
        const map = vowelMap || { a: 0, e: 0, i: 0, o: 0, u: 0 };
        const maxVowel = Math.max(...Object.values(map), 1);

        vowels.forEach(v => {
            const count = map[v] || 0;
            const pct = ((count / maxVowel) * 100).toFixed(0);
            const fillEl = document.getElementById(`vowel${v.toUpperCase()}`);
            const countEl = document.getElementById(`vowelCount${v.toUpperCase()}`);
            if (fillEl) fillEl.style.width = `${pct}%`;
            if (countEl) countEl.textContent = count;
        });
    }

    function resetResults() {
        metricWords.textContent = '0';
        metricUniqueWords.textContent = '0 unique words';
        metricCharsWithSpaces.textContent = '0';
        metricCharsNoSpaces.textContent = '0 without spaces';
        metricSentences.textContent = '0';
        metricParagraphs.textContent = '0 paragraphs';
        metricVowelsConsonants.textContent = '0 / 0';
        metricReadingTime.textContent = '0.0m';
        metricReadabilityScore.textContent = '0.0';
        metricReadabilityLevel.textContent = 'Standard';
        frequencyBarsChart.innerHTML = '<div class="empty-state">Run analysis to see word frequency chart.</div>';
        frequencyTableBody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No data available</td></tr>';
        longestWordList.innerHTML = '<span class="empty-badge">None</span>';
        shortestWordList.innerHTML = '<span class="empty-badge">None</span>';
        longestLength.textContent = '0';
        shortestLength.textContent = '0';
    }

    // 2. Search Word
    executeSearchBtn.addEventListener('click', async () => {
        const text = mainTextInput.value;
        const query = searchWordInput.value.trim();

        if (!text) {
            showToast('Please enter text to search.');
            return;
        }
        if (!query) {
            showToast('Please specify a word or phrase to find.');
            return;
        }

        try {
            const response = await fetch('/api/search', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    text: text,
                    query: query,
                    matchCase: searchMatchCase.checked,
                    wholeWord: searchWholeWord.checked,
                    sessionId: getSessionId()
                })
            });

            const res = await response.json();
            if (res.success && res.data) {
                const sr = res.data;
                if (sr.found) {
                    searchStatusText.innerHTML = `Found <span class="badge badge-primary">${sr.occurrences} occurrence(s)</span> of '<strong>${escapeHtml(sr.query)}</strong>'`;
                    if (sr.matchedLines && sr.matchedLines.length > 0) {
                        searchSnippetsContainer.classList.remove('hidden');
                        searchSnippetsList.innerHTML = sr.matchedLines.map(line =>
                            `<div class="snippet-item">${escapeHtml(line)}</div>`
                        ).join('');
                    } else {
                        searchSnippetsContainer.classList.add('hidden');
                    }
                } else {
                    searchStatusText.innerHTML = `Query '<strong>${escapeHtml(sr.query)}</strong>' was <span class="text-danger">not found</span>.`;
                    searchSnippetsContainer.classList.add('hidden');
                }
            }
        } catch (error) {
            console.error(error);
            showToast('Search failed: Network error.');
        }
    });

    // 3. Find and Replace
    executeReplaceBtn.addEventListener('click', async () => {
        const text = mainTextInput.value;
        const target = findTargetInput.value;
        const replacement = findReplacementInput.value;

        if (!text || !target) {
            showToast('Please enter source text and target to find.');
            return;
        }

        try {
            const response = await fetch('/api/replace', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    text: text,
                    target: target,
                    replacement: replacement,
                    matchCase: replaceMatchCase.checked,
                    sessionId: getSessionId()
                })
            });

            const res = await response.json();
            if (res.success && res.data) {
                replacePreviewText.value = res.data.updatedText;
                applyReplaceToMainBtn.disabled = false;
                showToast('Find & Replace preview ready!');
            }
        } catch (error) {
            console.error(error);
            showToast('Replace operation failed.');
        }
    });

    applyReplaceToMainBtn.addEventListener('click', () => {
        if (replacePreviewText.value) {
            mainTextInput.value = replacePreviewText.value;
            updateLiveStats();
            triggerAnalysis();
            applyReplaceToMainBtn.disabled = true;
            showToast('Updated main textarea and re-analyzed!');
        }
    });

    // 4. Case Conversion
    document.querySelectorAll('.dropdown-item[data-case]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const caseType = btn.getAttribute('data-case');
            const text = mainTextInput.value;
            if (!text.trim()) {
                showToast('Textarea is empty.');
                return;
            }

            try {
                const response = await fetch('/api/case-convert', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ text: text, caseType: caseType })
                });

                const res = await response.json();
                if (res.success && res.data) {
                    mainTextInput.value = res.data.convertedText;
                    updateLiveStats();
                    triggerAnalysis();
                    showToast(`Converted to ${caseType}!`);
                }
            } catch (error) {
                console.error(error);
                showToast('Case conversion failed.');
            }
        });
    });

    // 5. Save Analysis to Persistent History (StorageService in backend)
    saveHistoryBtn.addEventListener('click', async () => {
        const text = mainTextInput.value;
        if (!text.trim()) {
            showToast('Cannot save empty analysis.');
            return;
        }

        const words = parseInt(metricWords.textContent.replace(/,/g, '')) || 0;
        const chars = parseInt(metricCharsWithSpaces.textContent.replace(/,/g, '')) || 0;
        const sentences = parseInt(metricSentences.textContent.replace(/,/g, '')) || 0;
        const vowels = lastAnalysisResult ? lastAnalysisResult.totalVowels : 0;
        const consonants = lastAnalysisResult ? lastAnalysisResult.totalConsonants : 0;
        const readingEase = lastAnalysisResult ? lastAnalysisResult.fleschReadingEase : 0.0;
        const longestWord = lastAnalysisResult && lastAnalysisResult.longestWords && lastAnalysisResult.longestWords[0] ? lastAnalysisResult.longestWords[0] : '';
        const readingTime = lastAnalysisResult ? lastAnalysisResult.readingTimeMinutes : 0.0;
        const wordFreqs = lastAnalysisResult && lastAnalysisResult.wordFrequencies ? lastAnalysisResult.wordFrequencies.slice(0, 20) : [];

        try {
            const response = await fetch('/api/history', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    title: sourceBadge.textContent,
                    preview: text.length > 160 ? text.substring(0, 160) + '...' : text,
                    fullText: text,
                    wordCount: words,
                    charCount: chars,
                    sentenceCount: sentences,
                    vowelCount: vowels,
                    consonantCount: consonants,
                    readingEase: readingEase,
                    longestWord: longestWord,
                    readingTimeMinutes: readingTime,
                    wordFrequencies: wordFreqs,
                    sessionId: getSessionId()
                })
            });

            const res = await response.json();
            if (res.success) {
                showToast('Analysis permanently saved to disk history!');
            }
        } catch (error) {
            console.error(error);
            showToast('Failed to save to history.');
        }
    });

    // Load History List
    async function loadHistoryList() {
        historyListContainer.innerHTML = '<div class="empty-state">Loading history...</div>';
        try {
            const response = await fetch('/api/history');
            const res = await response.json();
            if (res.success && res.data) {
                const records = res.data;
                historyById = {};
                records.forEach(rec => { historyById[rec.id] = rec; });
                if (records.length === 0) {
                    historyListContainer.innerHTML = '<div class="empty-state">No saved analysis records found.</div>';
                    return;
                }

                historyListContainer.innerHTML = records.map(rec => `
                    <div class="history-card" data-id="${escapeHtml(rec.id)}">
                        <div class="history-meta">
                            <div class="history-title-row">
                                <h5>${escapeHtml(rec.title)}</h5>
                                <div class="history-actions">
                                    <button class="btn btn-sm btn-secondary btn-load-rec" data-id="${escapeHtml(rec.id)}" title="Load complete text">
                                        Load Text
                                    </button>
                                    <button class="btn btn-sm btn-outline text-danger btn-delete-rec" data-id="${escapeHtml(rec.id)}" title="Delete record">
                                        Delete
                                    </button>
                                </div>
                            </div>
                            <div class="history-stats">
                                ${escapeHtml(rec.savedAt)} &bull; 
                                <strong>${rec.wordCount}</strong> words &bull; 
                                <strong>${rec.charCount}</strong> chars &bull;
                                <strong>${rec.sentenceCount}</strong> sentences
                            </div>
                            <p class="text-muted" style="font-size: 0.8rem; margin-top: 4px;">${escapeHtml(rec.preview)}</p>
                        </div>
                    </div>
                `).join('');
            }
        } catch (error) {
            historyListContainer.innerHTML = '<div class="empty-state text-danger">Failed to load history.</div>';
        }
    }

    // History card item actions (Delete / Load)
    historyListContainer.addEventListener('click', async (e) => {
        const delBtn = e.target.closest('.btn-delete-rec');
        if (delBtn) {
            const id = delBtn.getAttribute('data-id');
            if (id && confirm('Delete this saved analysis record?')) {
                try {
                    const response = await fetch('/api/history', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ action: 'delete', id: id })
                    });
                    const res = await response.json();
                    if (res.success) {
                        showToast('Record deleted successfully.');
                        loadHistoryList();
                    } else {
                        showToast('Failed to delete record.');
                    }
                } catch (err) {
                    showToast('Delete failed.');
                }
            }
            return;
        }

        const loadBtn = e.target.closest('.btn-load-rec');
        if (loadBtn) {
            const id = loadBtn.getAttribute('data-id');
            const rec = historyById[id];
            const full = rec ? (rec.fullText || rec.preview || '') : '';
            if (full) {
                mainTextInput.value = full;
                sourceBadge.textContent = rec.title || 'History';
                sourceDetails.textContent = 'Restored complete document from SQLite';
                updateLiveStats();
                triggerAnalysis('Loaded from History');
                window.scrollTo({ top: 0, behavior: 'smooth' });
                showToast('Loaded complete text into editor.');
            }
        }
    });

    // Clear all history button
    const clearAllHistoryBtn = document.getElementById('clearAllHistoryBtn');
    if (clearAllHistoryBtn) {
        clearAllHistoryBtn.addEventListener('click', async () => {
            if (!confirm('Are you sure you want to delete ALL saved analysis records? This cannot be undone.')) {
                return;
            }
            try {
                const response = await fetch('/api/history', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ action: 'clear' })
                });
                const res = await response.json();
                if (res.success) {
                    showToast('All analysis history deleted.');
                    loadHistoryList();
                } else {
                    showToast('Failed to clear history.');
                }
            } catch (err) {
                showToast('Failed to clear history.');
            }
        });
    }

    if (clearSessionHistoryBtn) {
        clearSessionHistoryBtn.addEventListener('click', async () => {
            if (!confirm('Clear this session\'s activity log? Saved analyses are kept.')) {
                return;
            }
            try {
                const response = await fetch('/api/sessions', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ action: 'clear', sessionId: getSessionId() })
                });
                const res = await response.json();
                if (res.success) {
                    showToast('Session history cleared.');
                    loadSessionActivities();
                } else {
                    showToast(res.message || 'Failed to clear session history.');
                }
            } catch (err) {
                showToast('Failed to clear session history.');
            }
        });
    }

    refreshHistoryBtn.addEventListener('click', () => {
        loadHistoryList();
        loadSessionActivities();
    });

    frequencyTableBody.addEventListener('click', (e) => {
        const wordEl = e.target.closest('[data-word]');
        if (!wordEl) return;
        dictionaryWordInput.value = wordEl.getAttribute('data-word');
        activateTab('tabDictionary');
        lookupDictionaryWord();
    });

    lookupWordBtn.addEventListener('click', lookupDictionaryWord);
    dictionaryWordInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') lookupDictionaryWord();
    });

    async function lookupDictionaryWord() {
        const word = dictionaryWordInput.value.trim();
        if (!word) {
            showToast('Enter a word to look up.');
            return;
        }
        dictionaryResultBox.innerHTML = '<div class="empty-state">Looking up definition...</div>';
        try {
            const response = await fetch('/api/dictionary', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ word, sessionId: getSessionId() })
            });
            const res = await response.json();
            if (res.success && res.data) {
                renderDictionary(res.data);
            } else {
                dictionaryResultBox.innerHTML = `<div class="empty-state text-danger">${escapeHtml(res.message || 'No definition found.')}</div>`;
            }
        } catch (error) {
            dictionaryResultBox.innerHTML = '<div class="empty-state text-danger">Dictionary request failed. Is the server online?</div>';
        }
    }

    function renderDictionary(entry) {
        const source = entry.fromCache ? 'SQLite JDBC cache' : 'Free Dictionary API';
        let html = `
            <div class="dict-word-row">
                <span class="dict-word">${escapeHtml(entry.word)}</span>
                <span class="dict-phonetic">${escapeHtml(entry.phonetic || '')}</span>
                <span class="badge badge-primary">${escapeHtml(source)}</span>
            </div>
        `;
        (entry.meanings || []).forEach(meaning => {
            html += `<div class="dict-meaning"><div class="dict-pos">${escapeHtml(meaning.partOfSpeech)}</div>`;
            (meaning.definitions || []).forEach((item, idx) => {
                html += `<p class="dict-def">${idx + 1}. ${escapeHtml(item.definition)}</p>`;
                if (item.example) {
                    html += `<p class="dict-ex">“${escapeHtml(item.example)}”</p>`;
                }
            });
            html += '</div>';
        });
        dictionaryResultBox.innerHTML = html;
    }

    translateTextBtn.addEventListener('click', async () => {
        const text = mainTextInput.value.trim();
        if (!text) {
            showToast('Enter English text in the editor first.');
            return;
        }
        const langInput = document.querySelector('input[name="targetLang"]:checked');
        const targetLang = langInput ? langInput.value : 'hi';
        const label = targetLang === 'ml' ? 'Malayalam' : 'Hindi';
        translateTextBtn.disabled = true;
        translationMeta.textContent = `Translating to ${label}...`;
        try {
            const response = await fetch('/api/translate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ text, targetLang, sessionId: getSessionId() })
            });
            const res = await response.json();
            if (res.success && res.data) {
                lastTranslation = res.data.translatedText || '';
                translationOutput.value = lastTranslation;
                applyTranslationBtn.disabled = !lastTranslation;
                const src = res.data.fromCache ? 'SQLite JDBC cache' : 'Free translation API';
                translationMeta.textContent = `English → ${label} • ${src}`;
                showToast(`Translated to ${label}.`);
            } else {
                translationMeta.textContent = res.message || 'Translation failed.';
                showToast(res.message || 'Translation failed.');
            }
        } catch (error) {
            translationMeta.textContent = 'Could not reach the Java backend.';
            showToast('Translation request failed.');
        } finally {
            translateTextBtn.disabled = false;
        }
    });

    applyTranslationBtn.addEventListener('click', () => {
        if (!lastTranslation) return;
        mainTextInput.value = lastTranslation;
        updateLiveStats();
        triggerAnalysis();
        showToast('Editor replaced with translated paragraph.');
    });

    async function loadSessionActivities() {
        if (!sessionActivityList) return;
        sessionActivityList.innerHTML = '<div class="empty-state">Loading session activity...</div>';
        try {
            const dbRes = await fetch('/api/database');
            const dbJson = await dbRes.json();
            if (dbJson.success && dbJson.data) {
                const d = dbJson.data;
                databaseStatusText.textContent =
                    `${d.databaseType || 'SQLite'} • ${d.status || ''} • ${d.sessionsCount || 0} sessions • ${d.activitiesCount || 0} activities • ${d.analysesCount || 0} saved analyses`;
            }
        } catch (_) { /* status is optional */ }

        try {
            const response = await fetch('/api/sessions?sessionId=' + encodeURIComponent(getSessionId()));
            const res = await response.json();
            if (!res.success || !res.data) {
                sessionActivityList.innerHTML = '<div class="empty-state text-danger">Could not load session activity.</div>';
                return;
            }
            const activities = res.data.activities || [];
            if (activities.length === 0) {
                sessionActivityList.innerHTML = '<div class="empty-state">No activities in this session yet.</div>';
                return;
            }
            sessionActivityList.innerHTML = activities.map(act => `
                <div class="history-card activity-card">
                    <div class="history-meta">
                        <div class="activity-type">${escapeHtml(act.activityType)} • ${escapeHtml(act.createdAt || '')}</div>
                        <p class="text-muted" style="font-size: 0.8rem; margin-top: 4px;">${escapeHtml(act.sourceText || '')}</p>
                        <div class="activity-result">${escapeHtml(act.resultText || '')}</div>
                    </div>
                </div>
            `).join('');
        } catch (error) {
            sessionActivityList.innerHTML = '<div class="empty-state text-danger">Failed to load session activity.</div>';
        }
    }

    // 6. Export Report
    exportTxtBtn.addEventListener('click', () => exportReport('TEXT'));
    exportJsonBtn.addEventListener('click', () => exportReport('JSON'));

    async function exportReport(format) {
        const text = mainTextInput.value;
        if (!text.trim()) {
            showToast('Enter text before exporting.');
            return;
        }

        try {
            const response = await fetch('/api/export', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    text: text,
                    sourceName: sourceBadge.textContent,
                    format: format
                })
            });

            const res = await response.json();
            if (res.success && res.data) {
                currentReport = res.data;
                exportModalTitle.textContent = `Analysis Report (${res.data.format})`;
                exportModalContent.textContent = res.data.content;
                exportModal.classList.remove('hidden');
            }
        } catch (error) {
            console.error(error);
            showToast('Report export failed.');
        }
    }

    closeExportModalBtn.addEventListener('click', () => {
        exportModal.classList.add('hidden');
    });

    copyReportBtn.addEventListener('click', () => {
        if (currentReport) {
            navigator.clipboard.writeText(currentReport.content);
            showToast('Report copied to clipboard!');
        }
    });

    downloadReportFileBtn.addEventListener('click', () => {
        if (currentReport) {
            const blob = new Blob([currentReport.content], { type: 'text/plain;charset=utf-8' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = currentReport.fileName || 'analysis_report.txt';
            a.click();
            URL.revokeObjectURL(url);
            showToast(`Downloaded ${currentReport.fileName}`);
        }
    });

    // Download History CSV action
    const downloadHistoryCsvBtn = document.getElementById('downloadHistoryCsvBtn');
    if (downloadHistoryCsvBtn) {
        downloadHistoryCsvBtn.addEventListener('click', async () => {
            try {
                // Ensure fresh CSV export is written to laptop disk
                await fetch('/api/database/export-csv', { method: 'POST' });
                showToast('Exported latest history to data/exports/analyses.csv');
            } catch (ignored) {}
        });
    }

    // Helper: Escape HTML
    function escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function applyFontToEditor(font) {
        if (!font) return;
        if (font.stylesheetUrl && dynamicFontStylesheet) {
            dynamicFontStylesheet.href = font.stylesheetUrl;
        }
        document.documentElement.style.setProperty('--font-editor', font.cssStack);
        mainTextInput.style.fontFamily = font.cssStack;
        localStorage.setItem('lexicoreFontId', font.id);
        selectedFontId = font.id;
        if (fontStatusText) {
            fontStatusText.innerHTML = `Active typeface: <strong>${escapeHtml(font.name)}</strong> &mdash; applied via <code>POST /api/fonts</code>.`;
        }
        document.querySelectorAll('.font-card').forEach(card => {
            card.classList.toggle('selected', card.dataset.fontId === font.id);
        });
    }

    async function loadFontCatalog() {
        if (!fontCatalog) return;
        try {
            const response = await fetch('/api/fonts');
            const res = await response.json();
            const fonts = (res.success && res.data && res.data.fonts) ? res.data.fonts : [];
            if (!fonts.length) {
                fontCatalog.innerHTML = '<div class="empty-state">Font catalog unavailable.</div>';
                return;
            }
            fontCatalog.innerHTML = fonts.map(font => `
                <button type="button" class="font-card${font.id === selectedFontId ? ' selected' : ''}" data-font-id="${escapeHtml(font.id)}">
                    <div class="font-card-sample" style="font-family: ${escapeHtml(font.cssStack)}">Aa Bb</div>
                    <div class="font-card-name">${escapeHtml(font.name)}</div>
                    <div class="font-card-desc">${escapeHtml(font.description)}</div>
                </button>
            `).join('');

            const saved = fonts.find(f => f.id === selectedFontId) || fonts[0];
            applyFontToEditor(saved);
        } catch (error) {
            fontCatalog.innerHTML = '<div class="empty-state">Could not load fonts from the Java API.</div>';
        }
    }

    if (fontCatalog) {
        fontCatalog.addEventListener('click', (e) => {
            const card = e.target.closest('.font-card');
            if (!card) return;
            selectedFontId = card.dataset.fontId;
            document.querySelectorAll('.font-card').forEach(el => {
                el.classList.toggle('selected', el === card);
            });
        });
    }

    if (applyFontBtn) {
        applyFontBtn.addEventListener('click', async () => {
            try {
                const response = await fetch('/api/fonts', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ fontId: selectedFontId })
                });
                const res = await response.json();
                if (res.success && res.data) {
                    applyFontToEditor(res.data);
                    showToast('Applied ' + res.data.name + ' to the editor.');
                } else {
                    showToast(res.message || 'Could not apply font.');
                }
            } catch (error) {
                showToast('Font API request failed.');
            }
        });
    }

    updateLiveStats();
    loadFontCatalog();
});
