package com.textanalyzer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.textanalyzer.server.ApiResponse;
import com.textanalyzer.util.JsonUtil;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST API for GET/POST /api/fonts.
 * Returns a curated typeface catalog and applies the selected font to the editor.
 */
public class FontHandler extends BaseApiHandler {

    private static final Map<String, FontSpec> FONTS = new LinkedHashMap<>();

    static {
        FONTS.put("editorial", new FontSpec(
                "editorial", "Editorial Serif",
                "Cormorant Garamond",
                "'Cormorant Garamond', 'Times New Roman', serif",
                "https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap",
                "Refined book serif for long-form reading"));
        FONTS.put("garamond", new FontSpec(
                "garamond", "Classic Garamond",
                "EB Garamond",
                "'EB Garamond', Georgia, serif",
                "https://fonts.googleapis.com/css2?family=EB+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap",
                "Traditional manuscript proportions"));
        FONTS.put("baskerville", new FontSpec(
                "baskerville", "Libre Baskerville",
                "Libre Baskerville",
                "'Libre Baskerville', Georgia, serif",
                "https://fonts.googleapis.com/css2?family=Libre+Baskerville:ital,wght@0,400;0,700;1,400&display=swap",
                "Literary newspaper serif"));
        FONTS.put("playfair", new FontSpec(
                "playfair", "Playfair Display",
                "Playfair Display",
                "'Playfair Display', 'Times New Roman', serif",
                "https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&display=swap",
                "High-contrast luxury display"));
        FONTS.put("lora", new FontSpec(
                "lora", "Lora",
                "Lora",
                "'Lora', Georgia, serif",
                "https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400;0,500;0,600;0,700;1,400&display=swap",
                "Contemporary calligraphic serif"));
        FONTS.put("source-serif", new FontSpec(
                "source-serif", "Source Serif",
                "Source Serif 4",
                "'Source Serif 4', Georgia, serif",
                "https://fonts.googleapis.com/css2?family=Source+Serif+4:ital,opsz,wght@0,8..60,400;0,8..60,600;1,8..60,400&display=swap",
                "Quiet professional reading face"));
        FONTS.put("outfit", new FontSpec(
                "outfit", "Outfit",
                "Outfit",
                "'Outfit', 'Segoe UI', sans-serif",
                "https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700&display=swap",
                "Geometric sans for modern UI copy"));
        FONTS.put("cormorant-infant", new FontSpec(
                "cormorant-infant", "Cormorant Infant",
                "Cormorant Infant",
                "'Cormorant Infant', 'Times New Roman', serif",
                "https://fonts.googleapis.com/css2?family=Cormorant+Infant:ital,wght@0,400;0,600;0,700;1,400&display=swap",
                "Soft editorial infant serif"));
        FONTS.put("ibm-plex", new FontSpec(
                "ibm-plex", "IBM Plex Sans",
                "IBM Plex Sans",
                "'IBM Plex Sans', 'Helvetica Neue', sans-serif",
                "https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:ital,wght@0,400;0,500;0,600;1,400&display=swap",
                "Corporate humanist sans"));
        FONTS.put("plex-mono", new FontSpec(
                "plex-mono", "IBM Plex Mono",
                "IBM Plex Mono",
                "'IBM Plex Mono', 'Courier New', monospace",
                "https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:ital,wght@0,400;0,500;1,400&display=swap",
                "Monospaced drafts and code-like text"));
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();

        if ("GET".equals(method)) {
            sendJson(exchange, 200, ApiResponse.ok("Font catalog loaded", catalogJson()).toJson());
            return;
        }

        if ("POST".equals(method)) {
            Map<String, Object> req = JsonUtil.parseJsonObject(readRequestBody(exchange));
            String fontId = String.valueOf(req.getOrDefault("fontId", "editorial")).trim().toLowerCase();
            FontSpec spec = FONTS.get(fontId);
            if (spec == null) {
                sendJson(exchange, 400, ApiResponse.error("Unknown fontId. Use GET /api/fonts for the catalog.").toJson());
                return;
            }
            sendJson(exchange, 200, ApiResponse.ok("Font applied: " + spec.name, spec.toJson(true)).toJson());
            return;
        }

        sendJson(exchange, 405, ApiResponse.error("Only GET and POST are allowed on /api/fonts").toJson());
    }

    private static String catalogJson() {
        StringBuilder sb = new StringBuilder("{\"fonts\":[");
        int i = 0;
        for (FontSpec spec : FONTS.values()) {
            if (i++ > 0) sb.append(",");
            sb.append(spec.toJson(false));
        }
        sb.append("],\"defaultFontId\":\"editorial\"}");
        return sb.toString();
    }

    private static final class FontSpec {
        final String id;
        final String name;
        final String family;
        final String cssStack;
        final String stylesheetUrl;
        final String description;

        FontSpec(String id, String name, String family, String cssStack, String stylesheetUrl, String description) {
            this.id = id;
            this.name = name;
            this.family = family;
            this.cssStack = cssStack;
            this.stylesheetUrl = stylesheetUrl;
            this.description = description;
        }

        String toJson(boolean selected) {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"id\":\"").append(JsonUtil.escape(id)).append("\",");
            sb.append("\"name\":\"").append(JsonUtil.escape(name)).append("\",");
            sb.append("\"family\":\"").append(JsonUtil.escape(family)).append("\",");
            sb.append("\"cssStack\":\"").append(JsonUtil.escape(cssStack)).append("\",");
            sb.append("\"stylesheetUrl\":\"").append(JsonUtil.escape(stylesheetUrl)).append("\",");
            sb.append("\"description\":\"").append(JsonUtil.escape(description)).append("\"");
            if (selected) {
                sb.append(",\"selected\":true");
            }
            sb.append("}");
            return sb.toString();
        }
    }
}
