package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class GuiMarkdown extends GuiComponent {
    private static final Pattern IMG_PATTERN = Pattern.compile("^!\\[(.*?)\\]\\((.*?)\\)");
    public java.util.function.Consumer<String> onLinkClicked;
    private java.util.Map<String, Integer> anchors = new java.util.HashMap<>();
    private String rawText;
    public int textColor = 0xFFE0E0E6;
    public int colorH1 = 0xFF00E5FF;
    public int colorH2 = 0xFF00E676;
    public int colorH3 = 0xFFFFD600;
    private List<MarkdownNode> nodes = new ArrayList<>();
    private int colorLink = 0xFF29B6F6;
    private int colorCode = 0xFFFFAB40;
    private int colorQuote = 0xFFAAAAAA;
    private int colorSep = 0xFF444450;
    private int lineHeight = 10;
    private int indentWidth = 10;
    public GuiMarkdown(int x, int y, int width, String text) {
        super(x, y, width, 0);
        this.rawText = text;
        parseAndLayout();
    }
    public void setText(String text) {
        this.rawText = text;
        parseAndLayout();
    }
    public String getRawText() {
        return this.rawText;
    }
    public int getAnchorY(String id) {
        return anchors.getOrDefault(id, 0);
    }
    private void parseAndLayout() {
        nodes.clear();
        anchors.clear();
        this.height = 0;
        if (rawText == null || rawText.isEmpty()) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        String[] lines = rawText.replace("\r", "").split("\n");
        int currentY = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                currentY += 6;
                continue;
            }
            Matcher m = IMG_PATTERN.matcher(trimmed);
            if (m.find()) {
                String alt = m.group(1);
                String path = m.group(2);
                String[] parts = path.split(":", 2);
                if (parts.length == 2) {
                    ImageNode inode = new ImageNode(0, currentY, new ResourceLocation(parts[0], parts[1]), alt);
                    nodes.add(inode);
                    currentY += inode.h + 4;
                    continue;
                }
            }
            if (trimmed.equals("---") || trimmed.equals("***") || trimmed.equals("___")) {
                nodes.add(new SeparatorNode(0, currentY, width));
                currentY += lineHeight;
                continue;
            }
            if (line.startsWith("### ")) {
                String title = line.substring(4);
                anchors.put(title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""), currentY);
                currentY += addTextNode(fr, title, 0, currentY, width, colorH3, true, false, false);
                currentY += 4; continue;
            }
            if (line.startsWith("## ")) {
                String title = line.substring(3);
                anchors.put(title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""), currentY);
                currentY += addTextNode(fr, title, 0, currentY, width, colorH2, true, false, false);
                currentY += 4; continue;
            }
            if (line.startsWith("# ")) {
                String title = line.substring(2);
                anchors.put(title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""), currentY);
                currentY += addTextNode(fr, title, 0, currentY, width, colorH1, true, false, false);
                currentY += 6; continue;
            }
            if (line.startsWith("> ")) {
                int startY = currentY;
                int h = addTextNode(fr, line.substring(2), 8, currentY, width - 8, colorQuote, false, true, false);
                nodes.add(new BlockquoteBarNode(0, startY, 4, h));
                currentY += 2;
                continue;
            }
            int indent = 0;
            String rest = line;
            while (rest.startsWith("  ")) {
                indent++;
                rest = rest.substring(2);
            }
            if (rest.startsWith("- ") || rest.startsWith("* ")) {
                rest = rest.substring(2);
                int bx = indent * indentWidth;
                nodes.add(new BulletNode(bx, currentY, indent));
                currentY += addTextNode(fr, rest, bx + 12, currentY, width - bx - 12, textColor, false, false, false);
                currentY += 2;
                continue;
            }
            currentY += addTextNode(fr, line, 0, currentY, width, textColor, false, false, false);
            currentY += 2;
        }
        this.height = currentY;
    }
    private int addTextNode(FontRenderer fr, String text, int startX, int startY, int maxW, int baseColor, boolean isHeading, boolean isQuote, boolean isCode) {
        TextNode tnode = new TextNode();
        int currX = startX;
        int currY = startY;
        List<Segment> segments = parseInlineStyles(text);
        for (Segment seg : segments) {
            String[] tokens = seg.text.split("(?<= )|(?= )");
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                String mcFormat = "";
                if (isHeading || seg.bold) mcFormat += TextFormatting.BOLD;
                if (seg.italic) mcFormat += TextFormatting.ITALIC;
                int color = baseColor;
                if (seg.isLink) color = colorLink;
                if (seg.isCode) color = colorCode;
                String formatted = mcFormat + token;
                int w = com.x4yi.x4ui.client.gui.utils.FontWidthCache.getStringWidth(fr, formatted, "x4ui");
                if (!token.trim().isEmpty() && maxW > 0 && currX + w > startX + maxW) {
                    currX = startX;
                    currY += lineHeight;
                }
                tnode.words.add(new RenderWord(currX, currY, formatted, color, seg.isLink, seg.linkTarget));
                currX += w;
            }
        }
        nodes.add(tnode);
        return (currY - startY) + lineHeight;
    }
    private List<Segment> parseInlineStyles(String text) {
        List<Segment> result = new ArrayList<>();
        boolean bold = false, italic = false, code = false, link = false;
        StringBuilder current = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '`') {
                flushSegment(result, current, bold, italic, code, link, null);
                code = !code;
                i++;
            } else if (c == '*' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                flushSegment(result, current, bold, italic, code, link, null);
                bold = !bold;
                i += 2;
            } else if (c == '_' && i + 1 < text.length() && text.charAt(i + 1) == '_') {
                flushSegment(result, current, bold, italic, code, link, null);
                bold = !bold;
                i += 2;
            } else if (c == '_' || c == '*') {
                flushSegment(result, current, bold, italic, code, link, null);
                italic = !italic;
                i++;
            } else if (c == '[') {
                int endBracket = text.indexOf(']', i);
                if (endBracket != -1 && endBracket + 1 < text.length() && text.charAt(endBracket + 1) == '(') {
                    int endParen = text.indexOf(')', endBracket);
                    if (endParen != -1) {
                        flushSegment(result, current, bold, italic, code, link, null);
                        String linkText = text.substring(i + 1, endBracket);
                        String linkUrl = text.substring(endBracket + 2, endParen);
                        result.add(new Segment(linkText, bold, italic, code, true, linkUrl));
                        i = endParen + 1;
                        continue;
                    }
                }
                current.append(c);
                i++;
            } else {
                current.append(c);
                i++;
            }
        }
        flushSegment(result, current, bold, italic, code, link, null);
        return result;
    }
    private void flushSegment(List<Segment> result, StringBuilder current, boolean b, boolean i, boolean c, boolean l, String target) {
        if (current.length() > 0) {
            result.add(new Segment(current.toString(), b, i, c, l, target));
            current.setLength(0);
        }
    }
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) return true;
        if (mouseButton == 0) {
            int absX = getAbsoluteX();
            int absY = getAbsoluteY();
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            for (MarkdownNode node : nodes) {
                if (node instanceof TextNode) {
                    for (RenderWord w : ((TextNode) node).words) {
                        if (w.isLink) {
                            int sw = com.x4yi.x4ui.client.gui.utils.FontWidthCache.getStringWidth(fr, w.text, "x4ui");
                            int tx = absX + w.x;
                            int ty = absY + w.y;
                            if (mouseX >= tx && mouseX <= tx + sw && mouseY >= ty && mouseY <= ty + 10) {
                                if (onLinkClicked != null && w.linkTarget != null) {
                                    onLinkClicked.accept(w.linkTarget);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        for (MarkdownNode node : nodes) {
            node.draw(absX, absY, fr);
        }
    }
    private static class Segment {
        String text;
        boolean bold, italic, isCode, isLink;
        String linkTarget;
        Segment(String t, boolean b, boolean i, boolean c, boolean l, String tgt) { text = t; bold = b; italic = i; isCode = c; isLink = l; linkTarget = tgt; }
    }
    private static class RenderWord {
        int x, y;
        String text;
        int color;
        boolean isLink;
        String linkTarget;
        RenderWord(int x, int y, String text, int color, boolean isLink, String tgt) { this.x = x; this.y = y; this.text = text; this.color = color; this.isLink = isLink; this.linkTarget = tgt; }
    }
    private abstract class MarkdownNode {
        abstract void draw(int absX, int absY, FontRenderer fr);
    }
    private class TextNode extends MarkdownNode {
        List<RenderWord> words = new ArrayList<>();
        @Override
        void draw(int absX, int absY, FontRenderer fr) {
            for (RenderWord w : words) {
                fr.drawString(w.text, absX + w.x, absY + w.y, w.color);
                if (w.isLink) {
                    int sw = com.x4yi.x4ui.client.gui.utils.FontWidthCache.getStringWidth(fr, w.text, "x4ui");
                    Gui.drawRect(absX + w.x, absY + w.y + 9, absX + w.x + sw, absY + w.y + 10, w.color);
                }
            }
        }
    }
    private class SeparatorNode extends MarkdownNode {
        int x, y, w;
        SeparatorNode(int x, int y, int w) { this.x = x; this.y = y; this.w = w; }
        @Override
        void draw(int absX, int absY, FontRenderer fr) {
            Gui.drawRect(absX + x, absY + y + 4, absX + x + w, absY + y + 5, colorSep);
        }
    }
    private class BlockquoteBarNode extends MarkdownNode {
        int x, y, w, h;
        BlockquoteBarNode(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        @Override
        void draw(int absX, int absY, FontRenderer fr) {
            Gui.drawRect(absX + x, absY + y, absX + x + w, absY + y + h, colorQuote);
        }
    }
    private class BulletNode extends MarkdownNode {
        int x, y, indent;
        BulletNode(int x, int y, int indent) { this.x = x; this.y = y; this.indent = indent; }
        @Override
        void draw(int absX, int absY, FontRenderer fr) {
            int color = indent == 0 ? 0xFF00C853 : (indent == 1 ? 0xFF888892 : 0xFF555560);
            if (indent == 0) {
                Gui.drawRect(absX + x, absY + y + 3, absX + x + 4, absY + y + 7, color);
            } else {
                Gui.drawRect(absX + x + 1, absY + y + 4, absX + x + 3, absY + y + 6, color);
            }
        }
    }
    private class ImageNode extends MarkdownNode {
        int x, y, w, h;
        ResourceLocation loc;
        String alt;
        ImageNode(int x, int y, ResourceLocation loc, String alt) {
            this.x = x; this.y = y; this.loc = loc; this.alt = alt;
            this.w = 64; this.h = 64;
            if (alt.matches("\\d+x\\d+")) {
                String[] dims = alt.split("x");
                this.w = Integer.parseInt(dims[0]);
                this.h = Integer.parseInt(dims[1]);
            }
        }
        @Override
        void draw(int absX, int absY, FontRenderer fr) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
            GlStateManager.color(1, 1, 1, 1);
            Gui.drawModalRectWithCustomSizedTexture(absX + x, absY + y, 0, 0, w, h, w, h);
        }
    }
}