/*
 * Copyright (c) 2024, NullBot
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.osrschatlogger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

/**
 * Side panel for OSRS Chatlogger plugin with quick links.
 */
@Slf4j
public class OsrsChatloggerPanel extends PluginPanel
{
    private static final String WEBSITE_URL = "https://osrschatlogger.com";
    private static final String DISCORD_URL = "https://discord.gg/xAa3JXaVb5";
    private static final String GITHUB_URL = "https://github.com/NullBotOSRS";

    private static final int ICON_SIZE = 16;

    private final ChatSender chatSender;

    public OsrsChatloggerPanel(ChatSender chatSender)
    {
        super(false);
        this.chatSender = chatSender;

        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Create header with title and icon row
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create status panel
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel()
    {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        // Title label
        JLabel titleLabel = new JLabel("OSRS Chatlogger");
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Icon row panel
        JPanel iconRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        iconRow.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Website icon
        iconRow.add(createIconButton(createWebsiteIcon(), "Website", WEBSITE_URL));

        // Discord icon
        iconRow.add(createIconButton(createDiscordIcon(), "Discord", DISCORD_URL));

        // GitHub icon
        iconRow.add(createIconButton(createGithubIcon(), "GitHub", GITHUB_URL));

        headerPanel.add(iconRow, BorderLayout.EAST);

        return headerPanel;
    }

    private JLabel createIconButton(BufferedImage icon, String tooltip, String url)
    {
        JLabel label = new JLabel(new ImageIcon(icon));
        label.setToolTipText(tooltip);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e)
            {
                LinkBrowser.browse(url);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e)
            {
                label.setBorder(BorderFactory.createLineBorder(ColorScheme.BRAND_ORANGE, 1));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e)
            {
                label.setBorder(null);
            }
        });
        return label;
    }

    private JPanel createStatusPanel()
    {
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statusPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new javax.swing.BoxLayout(infoPanel, javax.swing.BoxLayout.Y_AXIS));
        infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Instructions
        JLabel instructionLabel = new JLabel("<html><body style='width: 180px'>" +
            "Configure your API key in the plugin settings to start syncing chat messages." +
            "</body></html>");
        instructionLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        instructionLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        infoPanel.add(instructionLabel);

        // Get API key link
        JLabel apiKeyLink = new JLabel("<html><u>Get your API key</u></html>");
        apiKeyLink.setForeground(ColorScheme.BRAND_ORANGE);
        apiKeyLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        apiKeyLink.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e)
            {
                LinkBrowser.browse(WEBSITE_URL + "/settings/api");
            }
        });
        infoPanel.add(apiKeyLink);

        statusPanel.add(infoPanel, BorderLayout.NORTH);

        return statusPanel;
    }

    /**
     * Create a simple globe/world icon for website link.
     */
    private BufferedImage createWebsiteIcon()
    {
        BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw globe circle
        g.setColor(new Color(100, 181, 246)); // Light blue
        g.fillOval(1, 1, ICON_SIZE - 2, ICON_SIZE - 2);

        // Draw meridian lines
        g.setColor(new Color(66, 133, 244)); // Darker blue
        g.drawOval(1, 1, ICON_SIZE - 3, ICON_SIZE - 3);
        g.drawLine(ICON_SIZE / 2, 1, ICON_SIZE / 2, ICON_SIZE - 2);
        g.drawLine(1, ICON_SIZE / 2, ICON_SIZE - 2, ICON_SIZE / 2);
        // Curved meridian
        g.drawArc(3, 1, ICON_SIZE - 6, ICON_SIZE - 3, 90, 180);
        g.drawArc(5, 1, ICON_SIZE - 6, ICON_SIZE - 3, -90, 180);

        g.dispose();
        return image;
    }

    /**
     * Create a simple chat bubble icon for Discord link.
     */
    private BufferedImage createDiscordIcon()
    {
        BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Discord blurple color
        g.setColor(new Color(88, 101, 242));

        // Draw rounded rectangle (chat bubble body)
        g.fillRoundRect(1, 2, ICON_SIZE - 2, ICON_SIZE - 5, 4, 4);

        // Draw small triangle at bottom for speech bubble
        int[] xPoints = {3, 6, 6};
        int[] yPoints = {ICON_SIZE - 3, ICON_SIZE - 3, ICON_SIZE - 1};
        g.fillPolygon(xPoints, yPoints, 3);

        // Draw two dots (simplified Discord logo eyes)
        g.setColor(Color.WHITE);
        g.fillOval(4, 6, 3, 3);
        g.fillOval(9, 6, 3, 3);

        g.dispose();
        return image;
    }

    /**
     * Create a simple code bracket icon for GitHub link.
     */
    private BufferedImage createGithubIcon()
    {
        BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // GitHub gray color
        g.setColor(new Color(110, 118, 129));

        // Draw circle background
        g.fillOval(0, 0, ICON_SIZE, ICON_SIZE);

        // Draw simplified octocat silhouette (circle with ears)
        g.setColor(new Color(36, 41, 46)); // Dark background
        g.fillOval(2, 3, ICON_SIZE - 4, ICON_SIZE - 4);

        // Draw face area (lighter)
        g.setColor(new Color(110, 118, 129));
        g.fillOval(4, 5, ICON_SIZE - 8, ICON_SIZE - 7);

        // Small ears
        g.setColor(new Color(36, 41, 46));
        int[] leftEarX = {3, 5, 6};
        int[] leftEarY = {5, 2, 5};
        g.fillPolygon(leftEarX, leftEarY, 3);
        int[] rightEarX = {13, 11, 10};
        int[] rightEarY = {5, 2, 5};
        g.fillPolygon(rightEarX, rightEarY, 3);

        g.dispose();
        return image;
    }
}
