package com.mycompany.hotelmanagementsystem;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * HotelGUI - the AWT user interface (extends Frame).
 * Two screens switched with a CardLayout:
 *   "landing" - welcome page with the room categories
 *   "app"     - booking form, dashboard figures and the reservation table
 * Plain AWT components look dated, so a few small custom-painted components
 * (FlatButton, Card, ReservationTable) are included at the bottom of this file.
 */
public class HotelGUI extends Frame {

    // ---- Palette: neutrals + one accent colour ----
    private static final Color BG = new Color(250, 250, 251);
    private static final Color TEXT = new Color(31, 41, 51);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color LINE = new Color(228, 231, 235);
    private static final Color SOFT = new Color(243, 245, 247);
    private static final Color TINT = new Color(232, 244, 244);
    private static final Color ACCENT = new Color(13, 115, 119);
    private static final Color ACCENT_HOVER = new Color(17, 134, 139);
    private static final Color ACCENT_DARK = new Color(10, 91, 94);

    // ---- Typography: Segoe UI / Georgia when installed, logical fonts otherwise ----
    private static final String SANS = pickFamily("Segoe UI", "SansSerif");
    private static final String SERIF = pickFamily("Georgia", "Serif");

    private final HotelManager manager;

    // Screen switcher
    private final CardLayout cardLayout = new CardLayout();
    private final Panel cards = new Panel(cardLayout);

    // Form components
    private final TextField nameField = new TextField();
    private final TextField contactField = new TextField();
    private final Choice roomChoice = new Choice();
    private final TextField dateField = new TextField(LocalDate.now().toString());
    private final TextField nightsField = new TextField("1");
    private final Label totalLabel = new Label("-");

    // Dashboard + table
    private final Label reservationsStat = new Label("0");
    private final Label freeStat = new Label("0");
    private final Label revenueStat = new Label("0");
    private final ReservationTable table = new ReservationTable();
    private final Label statusLabel = new Label("Ready.");

    public HotelGUI(HotelManager manager) {
        super("Alon Hotel");
        this.manager = manager;

        setBackground(BG);
        setForeground(TEXT);
        setLayout(new BorderLayout());

        cards.add(buildLanding(), "landing");
        cards.add(buildApp(), "app");
        add(cards, BorderLayout.CENTER);
        cardLayout.show(cards, "landing"); // start on the home screen

        // Fill the room dropdown from the manager's rooms
        for (Room room : manager.getRooms()) {
            roomChoice.add(room.toString());
        }

        // Live total preview
        roomChoice.addItemListener(e -> updateTotal());
        nightsField.addTextListener(e -> updateTotal());
        updateTotal();
        refreshAll();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        setSize(1100, 720);
        setLocationRelativeTo(null);
    }

    // =====================================================================
    //  LANDING SCREEN
    // =====================================================================

    private Panel buildLanding() {
        Page page = new Page(new BorderLayout(0, 34), new Insets(34, 72, 26, 72));

        // Hero: brand, headline, tagline and the main call to action
        Panel hero = new Panel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        c.insets = new Insets(0, 0, 40, 0);
        hero.add(label("Alon Hotel", serif(Font.BOLD, 22), ACCENT), c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 10, 0);
        hero.add(label("A quiet place to stay.", serif(Font.PLAIN, 54), TEXT), c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 26, 0);
        hero.add(label("Comfortable rooms, honest prices, and a front desk that is always open.",
                sans(Font.PLAIN, 16), MUTED), c);
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        hero.add(new FlatButton("Book a room", FlatButton.PRIMARY, 40,
                () -> cardLayout.show(cards, "app")), c);
        page.add(hero, BorderLayout.NORTH);

        // One tile per room category, built from the Room objects
        Panel tiles = new Panel(new GridLayout(1, 0, 16, 0));
        String lastType = "";
        for (Room room : manager.getRooms()) {
            if (room.getRoomType().equals(lastType)) {
                continue; // rooms are grouped by type; show each type once
            }
            lastType = room.getRoomType();
            tiles.add(buildRoomTile(room));
        }
        // Centre the tiles in the space between the hero and the footer
        Panel tilesWrap = new Panel(new GridBagLayout());
        GridBagConstraints tc = new GridBagConstraints();
        tc.weightx = 1;
        tc.fill = GridBagConstraints.HORIZONTAL;
        tilesWrap.add(tiles, tc);
        page.add(tilesWrap, BorderLayout.CENTER);

        page.add(label("Front desk open 24 hours   |   +63 917 555 0142", sans(Font.PLAIN, 12), MUTED),
                BorderLayout.SOUTH);
        return page;
    }

    /** One room-category tile; uses polymorphic getRoomType() / getAmenities(). */
    private Panel buildRoomTile(Room room) {
        Card tile = new Card(new BorderLayout(0, 16), 24, 0);

        Panel top = new Panel(new GridLayout(2, 1, 0, 4));
        top.add(label(room.getRoomType(), serif(Font.PLAIN, 24), TEXT));
        top.add(label(String.format("From PHP %,.0f per night", room.getRatePerNight()),
                sans(Font.BOLD, 14), ACCENT));

        Panel perks = new Panel(new GridLayout(0, 1, 0, 6));
        for (String perk : room.getAmenities().split(", ")) {
            String shown = perk.substring(0, 1).toUpperCase() + perk.substring(1);
            perks.add(label(shown, sans(Font.PLAIN, 13), MUTED));
        }
        Panel perksTop = new Panel(new BorderLayout()); // keeps rows tightly stacked at the top
        perksTop.add(perks, BorderLayout.NORTH);

        tile.add(top, BorderLayout.NORTH);
        tile.add(perksTop, BorderLayout.CENTER);
        return tile;
    }

    // =====================================================================
    //  BOOKING SCREEN
    // =====================================================================

    private Panel buildApp() {
        Page root = new Page(new BorderLayout(28, 20), new Insets(26, 36, 22, 36));
        root.add(buildAppHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.WEST);
        root.add(buildDashboard(), BorderLayout.CENTER);
        return root;
    }

    private Panel buildAppHeader() {
        Panel titles = new Panel(new GridLayout(2, 1, 0, 0));
        titles.add(label("Alon Hotel", serif(Font.BOLD, 24), ACCENT));
        titles.add(label("Reservations", sans(Font.PLAIN, 13), MUTED));

        Panel homeWrap = new Panel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        homeWrap.add(new FlatButton("Home", FlatButton.GHOST, 20,
                () -> cardLayout.show(cards, "landing")));

        Panel bar = new Panel(new BorderLayout());
        bar.add(titles, BorderLayout.CENTER);
        bar.add(homeWrap, BorderLayout.EAST);

        Panel header = new Panel(new BorderLayout(0, 16));
        header.add(bar, BorderLayout.NORTH);
        header.add(rule(), BorderLayout.SOUTH);
        return header;
    }

    private Panel buildForm() {
        Card card = new Card(new GridBagLayout(), 26, 350);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        styleField(nameField);
        styleField(contactField);
        styleField(dateField);
        styleField(nightsField);
        roomChoice.setFont(sans(Font.PLAIN, 13));
        roomChoice.setBackground(Color.WHITE);

        int row = 0;
        c.gridy = row++;
        c.insets = new Insets(0, 0, 20, 0);
        card.add(label("New reservation", serif(Font.PLAIN, 22), TEXT), c);

        c.gridy = row++;
        c.insets = new Insets(0, 0, 14, 0);
        card.add(fieldBlock("Guest name", nameField), c);
        c.gridy = row++;
        card.add(fieldBlock("Contact number", contactField), c);
        c.gridy = row++;
        card.add(fieldBlock("Room", roomChoice), c);

        Panel pair = new Panel(new GridLayout(1, 2, 12, 0));
        pair.add(fieldBlock("Check-in (yyyy-MM-dd)", dateField));
        pair.add(fieldBlock("Nights", nightsField));
        c.gridy = row++;
        card.add(pair, c);

        totalLabel.setFont(sans(Font.BOLD, 26));
        totalLabel.setForeground(ACCENT);
        c.gridy = row++;
        c.insets = new Insets(6, 0, 0, 0);
        card.add(label("Estimated total", sans(Font.PLAIN, 12), MUTED), c);
        c.gridy = row++;
        c.insets = new Insets(0, 0, 16, 0);
        card.add(totalLabel, c);

        c.gridy = row++;
        c.insets = new Insets(0, 0, 8, 0);
        card.add(new FlatButton("Book now", FlatButton.PRIMARY, 0, this::handleBook), c);
        c.gridy = row++;
        c.insets = new Insets(0, 0, 0, 0);
        card.add(new FlatButton("Clear form", FlatButton.SECONDARY, 0, () -> {
            clearForm();
            setStatus("Form cleared.");
        }), c);

        // Keep the card from stretching to the full window height
        Panel wrap = new Panel(new BorderLayout());
        wrap.add(card, BorderLayout.NORTH);
        return wrap;
    }

    private Panel buildDashboard() {
        Panel dash = new Panel(new BorderLayout(0, 16));

        // Three quiet figures at the top
        Panel stats = new Panel(new GridLayout(1, 3, 14, 0));
        stats.add(statCard("Reservations", reservationsStat));
        stats.add(statCard("Rooms free tonight", freeStat));
        stats.add(statCard("Booked revenue", revenueStat));
        dash.add(stats, BorderLayout.NORTH);

        // Table card: fixed header + scrollable rows
        Card tableCard = new Card(new BorderLayout(), 1, 0);
        tableCard.add(table.createHeader(), BorderLayout.NORTH);
        ScrollPane scroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        scroll.setBackground(Color.WHITE);
        scroll.add(table);
        tableCard.add(scroll, BorderLayout.CENTER);
        table.setOnOpen(this::handleDetails); // double-click opens details
        dash.add(tableCard, BorderLayout.CENTER);

        // Actions + status line
        Panel buttons = new Panel(new GridLayout(1, 2, 10, 0));
        buttons.add(new FlatButton("View details", FlatButton.SECONDARY, 22, this::handleDetails));
        buttons.add(new FlatButton("Cancel reservation", FlatButton.SECONDARY, 22, this::handleCancel));
        statusLabel.setFont(sans(Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);
        Panel actions = new Panel(new BorderLayout(14, 0));
        actions.add(buttons, BorderLayout.WEST);
        actions.add(statusLabel, BorderLayout.CENTER);
        dash.add(actions, BorderLayout.SOUTH);

        return dash;
    }

    private Card statCard(String caption, Label value) {
        Card card = new Card(new GridLayout(2, 1, 0, 2), 16, 0);
        value.setFont(sans(Font.PLAIN, 24));
        value.setForeground(TEXT);
        card.add(value);
        card.add(label(caption, sans(Font.PLAIN, 12), MUTED));
        return card;
    }

    /** A small grey caption above an input. */
    private Panel fieldBlock(String caption, Component field) {
        Panel block = new Panel(new BorderLayout(0, 5));
        block.add(label(caption, sans(Font.PLAIN, 12), MUTED), BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        return block;
    }

    private void styleField(TextField field) {
        field.setFont(sans(Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT);
    }

    // =====================================================================
    //  EVENT HANDLERS
    // =====================================================================

    private void handleBook() {
        try {
            int nights = Integer.parseInt(nightsField.getText().trim());
            LocalDate checkIn = LocalDate.parse(dateField.getText().trim());
            Room room = manager.getRooms().get(roomChoice.getSelectedIndex());

            Reservation r = manager.book(nameField.getText(), contactField.getText(),
                    room, checkIn, nights);

            refreshAll();
            clearForm();
            setStatus("Reservation " + r.getReference() + " confirmed.");
            showDialog("Reservation confirmed",
                    "Reservation: " + r.getReference() + "\n"
                    + "Guest: " + r.getGuestName() + "\n"
                    + "Room: " + room.getRoomNumber() + " (" + room.getRoomType() + ")\n"
                    + "Stay: " + r.getStayLabel() + "\n"
                    + String.format("Total: PHP %,.2f", r.getTotalCost()), "Done", null);

        } catch (NumberFormatException ex) {
            showDialog("Invalid input", "Number of nights must be a whole number.", "OK", null);
        } catch (DateTimeParseException ex) {
            showDialog("Invalid date", "Use the format yyyy-MM-dd, for example 2026-12-24.", "OK", null);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showDialog("Cannot book", ex.getMessage(), "OK", null);
        }
    }

    private void handleDetails() {
        Reservation r = getSelectedReservation();
        if (r == null) {
            return;
        }
        Room room = r.getRoom();
        showDialog("Reservation " + r.getReference(),
                "Guest: " + r.getGuestName() + "\n"
                + "Contact: " + r.getContact() + "\n"
                + "Room: " + room.getRoomNumber() + " (" + room.getRoomType() + ")\n"
                + "Includes: " + room.getAmenities() + "\n" // polymorphic call
                + "Check-in: " + r.getCheckInLabel() + "\n"
                + "Check-out: " + r.getCheckOutLabel() + "\n"
                + "Nights: " + r.getNights() + "\n"
                + String.format("Total: PHP %,.2f", r.getTotalCost()), "Close", null);
    }

    private void handleCancel() {
        Reservation r = getSelectedReservation();
        if (r == null) {
            return;
        }
        boolean confirmed = showDialog("Cancel reservation",
                "Cancel " + r.getReference() + " for " + r.getGuestName() + "?\n"
                + "This cannot be undone.", "Cancel reservation", "Keep it");
        if (confirmed) {
            manager.cancel(r.getId());
            refreshAll();
            setStatus("Reservation " + r.getReference() + " cancelled.");
        }
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    /** Returns the reservation highlighted in the table, or warns if none. */
    private Reservation getSelectedReservation() {
        int index = table.getSelectedIndex();
        if (index < 0) {
            showDialog("No selection", "Select a reservation from the table first.", "OK", null);
            return null;
        }
        return manager.getReservations().get(index);
    }

    /** Reloads the table and the three dashboard figures. */
    private void refreshAll() {
        table.setRows(manager.getReservations());
        reservationsStat.setText(String.valueOf(manager.getReservations().size()));
        freeStat.setText(manager.countAvailableToday() + " of " + manager.getRooms().size());
        revenueStat.setText(String.format("PHP %,.0f", manager.getTotalRevenue()));
    }

    private void clearForm() {
        nameField.setText("");
        contactField.setText("");
        dateField.setText(LocalDate.now().toString());
        nightsField.setText("1");
        roomChoice.select(0);
        updateTotal();
    }

    /** Recomputes the price preview using the selected room's own pricing rule. */
    private void updateTotal() {
        try {
            int nights = Integer.parseInt(nightsField.getText().trim());
            int index = roomChoice.getSelectedIndex();
            if (nights < 1 || index < 0) {
                totalLabel.setText("-");
                return;
            }
            Room room = manager.getRooms().get(index);
            totalLabel.setText(String.format("PHP %,.2f", room.calculateTotal(nights)));
        } catch (NumberFormatException ex) {
            totalLabel.setText("-");
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Shows a modal AWT Dialog. Lines written as "Key: value" are laid out as two columns.
     *
     * @param backText text of the secondary button, or null for a single-button dialog
     * @return true if the primary button was pressed
     */
    private boolean showDialog(String title, String message, String okText, String backText) {
        final boolean[] result = {false};
        final Dialog dialog = new Dialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setBackground(Color.WHITE);
        dialog.setResizable(false);

        Panel body = new Page(new GridBagLayout(), new Insets(28, 32, 10, 32)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(d.width, 420);
                return d;
            }
        };
        body.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 18, 0);
        body.add(label(title, serif(Font.PLAIN, 22), TEXT), c);

        int row = 1;
        for (String line : message.split("\n")) {
            int split = line.indexOf(": ");
            c.gridy = row++;
            if (split > 0 && split < 16) {
                c.gridx = 0;
                c.gridwidth = 1;
                c.weightx = 0;
                c.insets = new Insets(0, 0, 9, 22);
                body.add(label(line.substring(0, split), sans(Font.PLAIN, 13), MUTED), c);
                c.gridx = 1;
                c.weightx = 1;
                c.insets = new Insets(0, 0, 9, 0);
                body.add(label(line.substring(split + 2), sans(Font.PLAIN, 13), TEXT), c);
            } else {
                c.gridx = 0;
                c.gridwidth = 2;
                c.weightx = 1;
                c.insets = new Insets(0, 0, 9, 0);
                body.add(label(line, sans(Font.PLAIN, 13), TEXT), c);
            }
        }

        Panel actions = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 16));
        if (backText != null) {
            actions.add(new FlatButton(backText, FlatButton.SECONDARY, 22, dialog::dispose));
        }
        actions.add(new FlatButton(okText, FlatButton.PRIMARY, 26, () -> {
            result[0] = true;
            dialog.dispose();
        }));

        dialog.add(body, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dialog.dispose();
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true); // blocks until the dialog is closed
        return result[0];
    }

    // ---- Small static helpers shared by the UI code ----

    private static String pickFamily(String preferred, String fallback) {
        for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (family.equalsIgnoreCase(preferred)) {
                return preferred;
            }
        }
        return fallback;
    }

    private static Font sans(int style, int size) {
        return new Font(SANS, style, size);
    }

    private static Font serif(int style, int size) {
        return new Font(SERIF, style, size);
    }

    private static Label label(String text, Font font, Color color) {
        Label l = new Label(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /** A 1-pixel horizontal divider line. */
    private static Panel rule() {
        Panel p = new Panel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1, 1);
            }
        };
        p.setBackground(LINE);
        return p;
    }

    private static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    // =====================================================================
    //  CUSTOM COMPONENTS (nested, so the project stays at 5 files)
    // =====================================================================

    /** A panel with inner padding and a transparent background. */
    private static class Page extends Panel {
        private final Insets padding;

        Page(LayoutManager layout, Insets padding) {
            super(layout);
            this.padding = padding;
        }

        @Override
        public Insets getInsets() {
            return padding;
        }
    }

    /** A white panel with a thin border; optional fixed width. */
    private static class Card extends Panel {
        private final int pad;
        private final int fixedWidth;

        Card(LayoutManager layout, int pad, int fixedWidth) {
            super(layout);
            this.pad = pad;
            this.fixedWidth = fixedWidth;
            setBackground(Color.WHITE);
        }

        @Override
        public Insets getInsets() {
            return new Insets(pad, pad, pad, pad);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            if (fixedWidth > 0) {
                d.width = fixedWidth;
            }
            return d;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(LINE);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    /** A flat, rounded button with hover and pressed states. */
    private static class FlatButton extends Canvas {
        static final int PRIMARY = 0;
        static final int SECONDARY = 1;
        static final int GHOST = 2;

        private final String text;
        private final int style;
        private final int padX;
        private final Runnable action;
        private boolean hover;
        private boolean pressed;

        FlatButton(String text, int style, int padX, Runnable action) {
            this.text = text;
            this.style = style;
            this.padX = padX > 0 ? padX : 24;
            this.action = action;
            setFont(sans(style == PRIMARY ? Font.BOLD : Font.PLAIN, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    boolean fire = pressed && hover;
                    pressed = false;
                    repaint();
                    if (fire) {
                        FlatButton.this.action.run();
                    }
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(text) + 2 * padX, 40);
        }

        @Override
        public void update(Graphics g) {
            paint(g); // skip the default clear to avoid flicker
        }

        @Override
        public void paint(Graphics g0) {
            Graphics2D g = smooth(g0);
            int w = getWidth();
            int h = getHeight();

            Color parentBg = getParent() != null ? getParent().getBackground() : Color.WHITE;
            g.setColor(parentBg);
            g.fillRect(0, 0, w, h);

            Color fill;
            Color border = null;
            Color fg;
            if (style == PRIMARY) {
                fill = pressed ? ACCENT_DARK : hover ? ACCENT_HOVER : ACCENT;
                fg = Color.WHITE;
            } else if (style == SECONDARY) {
                fill = hover ? SOFT : Color.WHITE;
                border = LINE;
                fg = TEXT;
            } else {
                fill = hover ? SOFT : parentBg;
                fg = hover ? TEXT : MUTED;
            }

            g.setColor(fill);
            g.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);
            if (border != null) {
                g.setColor(border);
                g.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
            }

            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.setColor(fg);
            g.drawString(text, (w - fm.stringWidth(text)) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
        }
    }

    /** Custom-painted reservation table: two-line rows, hover and selection tint. */
    private static class ReservationTable extends Canvas {
        private static final int ROW_H = 60;
        private static final int PAD = 24;

        private java.util.List<Reservation> rows = new java.util.ArrayList<>();
        private int selected = -1;
        private int hover = -1;
        private Runnable onOpen;

        ReservationTable() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int i = e.getY() / ROW_H;
                    selected = (i >= 0 && i < rows.size()) ? i : -1;
                    repaint();
                    if (e.getClickCount() == 2 && selected >= 0 && onOpen != null) {
                        onOpen.run();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                    repaint();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int i = e.getY() / ROW_H;
                    i = (i >= 0 && i < rows.size()) ? i : -1;
                    if (i != hover) {
                        hover = i;
                        repaint();
                    }
                }
            });
        }

        void setOnOpen(Runnable onOpen) {
            this.onOpen = onOpen;
        }

        int getSelectedIndex() {
            return selected;
        }

        void setRows(java.util.List<Reservation> data) {
            rows = new java.util.ArrayList<>(data);
            selected = -1;
            hover = -1;
            invalidate();
            if (getParent() != null) {
                getParent().validate(); // resize inside the ScrollPane
            }
            repaint();
        }

        // Column positions shared by the header and the rows
        private static int colRoom(int w) {
            return (int) (w * 0.36);
        }

        private static int colStay(int w) {
            return (int) (w * 0.52);
        }

        @Override
        public Dimension getPreferredSize() {
            int w = 600;
            if (getParent() instanceof ScrollPane) {
                w = Math.max(400, ((ScrollPane) getParent()).getViewportSize().width);
            }
            return new Dimension(w, Math.max(rows.size() * ROW_H, 120));
        }

        @Override
        public void update(Graphics g) {
            paint(g);
        }

        @Override
        public void paint(Graphics g0) {
            Graphics2D g = smooth(g0);
            int w = getWidth();
            int h = getHeight();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);

            if (rows.isEmpty()) {
                g.setFont(sans(Font.PLAIN, 14));
                g.setColor(TEXT);
                g.drawString("No reservations yet", PAD, 44);
                g.setFont(sans(Font.PLAIN, 13));
                g.setColor(MUTED);
                g.drawString("Fill in the form and press Book now.", PAD, 66);
                return;
            }

            Font main = sans(Font.PLAIN, 14);
            Font small = sans(Font.PLAIN, 12);
            Font bold = sans(Font.BOLD, 14);

            for (int i = 0; i < rows.size(); i++) {
                Reservation r = rows.get(i);
                int y = i * ROW_H;

                if (i == selected) {
                    g.setColor(TINT);
                    g.fillRect(0, y, w, ROW_H);
                    g.setColor(ACCENT);
                    g.fillRect(0, y, 3, ROW_H);
                } else if (i == hover) {
                    g.setColor(SOFT);
                    g.fillRect(0, y, w, ROW_H);
                }

                int base1 = y + 26;
                int base2 = y + 46;
                int guestMax = colRoom(w) - PAD - 16;
                int roomMax = colStay(w) - colRoom(w) - 16;

                // Guest
                g.setFont(main);
                g.setColor(TEXT);
                g.drawString(fit(g.getFontMetrics(), r.getGuestName(), guestMax), PAD, base1);
                g.setFont(small);
                g.setColor(MUTED);
                g.drawString(fit(g.getFontMetrics(), r.getReference() + "   " + r.getContact(), guestMax),
                        PAD, base2);

                // Room
                g.setFont(main);
                g.setColor(TEXT);
                g.drawString("Room " + r.getRoom().getRoomNumber(), colRoom(w), base1);
                g.setFont(small);
                g.setColor(MUTED);
                g.drawString(fit(g.getFontMetrics(), r.getRoom().getRoomType(), roomMax), colRoom(w), base2);

                // Stay
                g.setFont(main);
                g.setColor(TEXT);
                g.drawString(r.getStayLabel(), colStay(w), base1);
                g.setFont(small);
                g.setColor(MUTED);
                g.drawString(r.getNightsLabel(), colStay(w), base2);

                // Total (right aligned)
                String total = String.format("PHP %,.2f", r.getTotalCost());
                g.setFont(bold);
                g.setColor(TEXT);
                g.drawString(total, w - PAD - g.getFontMetrics().stringWidth(total), base1);

                g.setColor(LINE);
                g.drawLine(0, y + ROW_H - 1, w, y + ROW_H - 1);
            }
        }

        /** Header strip drawn above the scrolling rows so it stays visible. */
        Canvas createHeader() {
            return new Canvas() {
                {
                    setBackground(Color.WHITE);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(100, 40);
                }

                @Override
                public void update(Graphics g) {
                    paint(g);
                }

                @Override
                public void paint(Graphics g0) {
                    Graphics2D g = smooth(g0);
                    int w = getWidth();
                    int h = getHeight();
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, w, h);
                    g.setFont(sans(Font.PLAIN, 12));
                    g.setColor(MUTED);
                    FontMetrics fm = g.getFontMetrics();
                    int base = (h - fm.getHeight()) / 2 + fm.getAscent();
                    g.drawString("Guest", PAD, base);
                    g.drawString("Room", colRoom(w), base);
                    g.drawString("Stay", colStay(w), base);
                    g.drawString("Total", w - PAD - fm.stringWidth("Total"), base);
                    g.setColor(LINE);
                    g.drawLine(0, h - 1, w, h - 1);
                }
            };
        }

        private static String fit(FontMetrics fm, String s, int max) {
            if (fm.stringWidth(s) <= max) {
                return s;
            }
            while (s.length() > 1 && fm.stringWidth(s + "...") > max) {
                s = s.substring(0, s.length() - 1);
            }
            return s + "...";
        }
    }
}