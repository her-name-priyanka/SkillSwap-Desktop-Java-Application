import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/*
 * SkillSwap - Peer-to-Peer Community Skill & Knowledge Exchange Platform
 * Hackathon Problem 5
 * Single-file Java Swing application
 * Data is saved locally in skillswap_data.dat
 */
public class SkillSwapApp extends JFrame {

    // ============================================================
    // DATA CLASSES - unchanged so existing skillswap_data.dat works
    // ============================================================
    static class User implements Serializable {
        String name, email, bio, experience, availability;
        List<String> teachSkills = new ArrayList<>();
        List<String> learnSkills = new ArrayList<>();
        double rating = 0;
        int ratingCount = 0;
        int completedSessions = 0;

        User(String name, String email, String bio, String experience,
             String availability, List<String> teachSkills, List<String> learnSkills) {
            this.name = name;
            this.email = email;
            this.bio = bio;
            this.experience = experience;
            this.availability = availability;
            this.teachSkills.addAll(teachSkills);
            this.learnSkills.addAll(learnSkills);
        }

        double getRating() { return ratingCount == 0 ? 0 : rating; }
    }

    static class Session implements Serializable {
        String learner, teacher, skill, date, time, status;

        Session(String learner, String teacher, String skill, String date, String time) {
            this.learner = learner;
            this.teacher = teacher;
            this.skill = skill;
            this.date = date;
            this.time = time;
            this.status = "Pending";
        }
    }

    static class Note implements Serializable {
        String author, partner, title, content, link;

        Note(String author, String partner, String title, String content, String link) {
            this.author = author;
            this.partner = partner;
            this.title = title;
            this.content = content;
            this.link = link;
        }
    }

    static class AppData implements Serializable {
        List<User> users = new ArrayList<>();
        List<Session> sessions = new ArrayList<>();
        List<Note> notes = new ArrayList<>();
    }

    // ============================================================
    // THEME
    // ============================================================
    static final String DATA_FILE = "skillswap_data.dat";

    static final Color BG = new Color(246, 248, 252);
    static final Color WHITE = Color.WHITE;
    static final Color NAVY = new Color(31, 38, 65);
    static final Color TEXT = new Color(55, 63, 85);
    static final Color MUTED = new Color(112, 120, 142);
    static final Color BORDER = new Color(226, 230, 240);
    static final Color PURPLE = new Color(105, 78, 220);
    static final Color PURPLE_DARK = new Color(79, 56, 174);
    static final Color BLUE = new Color(61, 126, 246);
    static final Color CYAN = new Color(30, 177, 190);
    static final Color GREEN = new Color(41, 174, 117);
    static final Color ORANGE = new Color(239, 145, 57);
    static final Color PINK = new Color(222, 82, 145);
    static final Color RED = new Color(215, 79, 87);
    static final Color PURPLE_LIGHT = new Color(239, 235, 255);
    static final Color BLUE_LIGHT = new Color(233, 241, 255);
    static final Color GREEN_LIGHT = new Color(231, 249, 241);
    static final Color ORANGE_LIGHT = new Color(255, 244, 229);
    static final Color PINK_LIGHT = new Color(255, 236, 246);
    static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);

    AppData data;
    User currentUser;
    JPanel mainPanel;
    JLabel welcomeLabel;
    JLabel statsLabel;

    // ============================================================
    // CUSTOM COMPONENTS
    // ============================================================
    static class RoundBorder extends AbstractBorder {
        final Color color; final int radius;
        RoundBorder(Color color, int radius) { this.color = color; this.radius = radius; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(10, 14, 10, 14); }
    }

    static class FillPanel extends JPanel {
        Color a, b;
        FillPanel(Color a, Color b) { this.a = a; this.b = b; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, a, getWidth(), getHeight(), b));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class ShadowPanel extends JPanel {
        ShadowPanel(LayoutManager lm) { super(lm); setOpaque(true); setBackground(WHITE); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(30, 40, 70, 18));
            g2.fillRoundRect(4, 5, getWidth() - 8, getHeight() - 7, 20, 20);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ============================================================
    // CONSTRUCTOR / MAIN
    // ============================================================
    public SkillSwapApp() {
        data = loadData();
        setTitle("SkillSwap • Peer Learning Platform");
        setSize(1180, 760);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(createAppIcon());
        showWelcome();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            UIManager.put("TextField.font", FONT);
            UIManager.put("TextArea.font", FONT);
            UIManager.put("ComboBox.font", FONT);
            UIManager.put("Label.font", FONT);
            UIManager.put("OptionPane.messageFont", FONT);
            UIManager.put("OptionPane.buttonFont", FONT_BOLD);
            new SkillSwapApp().setVisible(true);
        });
    }

    Image createAppIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(PURPLE); g.fillRoundRect(2, 2, 60, 60, 18, 18);
        g.setColor(Color.WHITE); g.setFont(new Font("Segoe UI", Font.BOLD, 32));
        g.drawString("S", 18, 43); g.dispose();
        return img;
    }

    // ============================================================
    // DATA STORAGE
    // ============================================================
    static AppData loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return new AppData();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (AppData) in.readObject();
        } catch (Exception e) {
            System.out.println("Starting with new local data.");
            return new AppData();
        }
    }

    void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(data);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save data:\n" + e.getMessage(), "Storage Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // COMMON UI HELPERS
    // ============================================================
    JLabel label(String text, int size, FontStyle style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style == FontStyle.BOLD ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(color);
        return l;
    }
    enum FontStyle { PLAIN, BOLD }

    JButton button(String text) { return button(text, PURPLE); }

    JButton button(String text, Color color) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? color.darker() : getModel().isRollover() ? color.brighter() : color;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    JButton outlineButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD); b.setForeground(PURPLE); b.setBackground(WHITE);
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new RoundBorder(new Color(205, 197, 246), 14));
        return b;
    }

    void styleField(JTextField f) {
        f.setFont(FONT); f.setForeground(TEXT); f.setBackground(WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(BORDER, 12), new EmptyBorder(7, 12, 7, 12)));
    }

    void styleArea(JTextArea a) {
        a.setFont(FONT); a.setForeground(TEXT); a.setBackground(WHITE); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(9, 11, 9, 11));
    }

    void styleCombo(JComboBox<?> c) {
        c.setFont(FONT); c.setForeground(TEXT); c.setBackground(WHITE);
        c.setBorder(new RoundBorder(BORDER, 12));
    }

    JPanel card() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(BORDER, 18), new EmptyBorder(18, 20, 18, 20)));
        return p;
    }

    JPanel pill(String text, Color bg, Color fg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3));
        p.setBackground(bg);
        p.setBorder(new RoundBorder(bg, 18));
        JLabel l = label(text, 12, FontStyle.BOLD, fg);
        p.add(l);
        return p;
    }

    JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.getVerticalScrollBar().setUnitIncrement(16);
        s.setBackground(BG);
        return s;
    }

    JPanel page(String title, String subtitle) {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.add(topBar(title, subtitle), BorderLayout.NORTH);
        return root;
    }

    JPanel topBar(String title, String subtitle) {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(WHITE);
        top.setBorder(new EmptyBorder(18, 28, 18, 28));
        JPanel left = new JPanel(); left.setOpaque(false); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(label(title, 25, FontStyle.BOLD, NAVY));
        if (subtitle != null && !subtitle.isEmpty()) {
            left.add(Box.createVerticalStrut(3));
            left.add(label(subtitle, 13, FontStyle.PLAIN, MUTED));
        }
        top.add(left, BorderLayout.WEST);
        if (currentUser != null) {
            JPanel user = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); user.setOpaque(false);
            user.add(pill(initials(currentUser.name), PURPLE_LIGHT, PURPLE));
            user.add(label(currentUser.name, 13, FontStyle.BOLD, TEXT));
            top.add(user, BorderLayout.EAST);
        }
        return top;
    }

    JPanel sidebar() {
        JPanel side = new JPanel();
        side.setBackground(NAVY);
        side.setBorder(new EmptyBorder(24, 15, 20, 15));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JLabel logo = label("SkillSwap", 24, FontStyle.BOLD, WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(Box.createVerticalStrut(5)); side.add(logo);
        JLabel sub = label("LEARN • SHARE • GROW", 10, FontStyle.BOLD, new Color(180, 190, 220));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT); side.add(Box.createVerticalStrut(4)); side.add(sub);
        side.add(Box.createVerticalStrut(30));

        addNav(side, "Dashboard", () -> showDashboard(), true);
        addNav(side, "My Profile", () -> showProfile(), false);
        addNav(side, "Find Partners", () -> showMatches(), false);
        addNav(side, "Schedule Session", () -> showSchedule(), false);
        addNav(side, "My Sessions", () -> showSessions(), false);
        addNav(side, "Notes & Resources", () -> showNotes(), false);
        addNav(side, "Ratings & Badges", () -> showRatings(), false);

        side.add(Box.createVerticalGlue());
        JPanel line = new JPanel(); line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); line.setBackground(new Color(255,255,255,45));
        side.add(line); side.add(Box.createVerticalStrut(15));
        JButton logout = navButton("Log out"); logout.setForeground(new Color(255,190,200));
        logout.addActionListener(e -> { currentUser = null; showProfiles(); });
        side.add(logout);
        return side;
    }

    void addNav(JPanel side, String text, Runnable action, boolean selected) {
        JButton b = navButton(text);
        if (selected) b.setBackground(new Color(255,255,255,35));
        b.addActionListener(e -> action.run());
        side.add(b); side.add(Box.createVerticalStrut(5));
    }

    JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(FONT_BOLD); b.setForeground(new Color(228,232,247));
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setOpaque(true); b.setBackground(new Color(255,255,255,25)); }
            public void mouseExited(MouseEvent e) { b.setOpaque(false); }
        });
        return b;
    }

    JPanel contentWithSidebar(String title, String subtitle) {
        JPanel root = page(title, subtitle);
        root.add(sidebar(), BorderLayout.WEST);
        JPanel content = new JPanel(new BorderLayout()); content.setBackground(BG);
        content.setBorder(new EmptyBorder(22, 24, 22, 24));
        root.add(content, BorderLayout.CENTER);
        return content;
    }

    String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "S";
        String[] p = name.trim().split("\\s+");
        return p.length == 1 ? p[0].substring(0,1).toUpperCase() : (p[0].substring(0,1) + p[p.length-1].substring(0,1)).toUpperCase();
    }

    void setScreen(JPanel panel) {
        setContentPane(panel); revalidate(); repaint();
    }

    // ============================================================
    // WELCOME
    // ============================================================
    void showWelcome() {
        FillPanel root = new FillPanel(new Color(74, 57, 174), new Color(46, 154, 190));
        root.setLayout(new GridBagLayout());
        JPanel glass = new JPanel(); glass.setOpaque(false); glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));
        glass.setBorder(new EmptyBorder(30, 55, 35, 55));

        JLabel logo = label("SKILLSWAP", 48, FontStyle.BOLD, WHITE); logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tagline = label("Learn from peers. Share what you know.", 20, FontStyle.PLAIN, new Color(235,238,255)); tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel desc = label("A simple community platform for exchanging skills, knowledge and opportunities.", 14, FontStyle.PLAIN, new Color(225,240,250)); desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton start = button("Get Started  →", WHITE); start.setForeground(PURPLE); start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.addActionListener(e -> showProfiles());

        glass.add(logo); glass.add(Box.createVerticalStrut(12)); glass.add(tagline); glass.add(Box.createVerticalStrut(10));
        glass.add(desc); glass.add(Box.createVerticalStrut(30)); glass.add(start);
        root.add(glass);
        setScreen(root);
    }

    // ============================================================
    // PROFILE SELECTION
    // ============================================================
    void showProfiles() {
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(BG);
        JPanel head = new JPanel(new BorderLayout()); head.setBackground(WHITE); head.setBorder(new EmptyBorder(24, 34, 20, 34));
        JPanel hl = new JPanel(); hl.setOpaque(false); hl.setLayout(new BoxLayout(hl, BoxLayout.Y_AXIS));
        hl.add(label("Welcome to SkillSwap", 28, FontStyle.BOLD, NAVY));
        hl.add(Box.createVerticalStrut(5)); hl.add(label("Choose a profile or create a new one to continue", 14, FontStyle.PLAIN, MUTED));
        head.add(hl, BorderLayout.WEST); root.add(head, BorderLayout.NORTH);

        JPanel center = new JPanel(); center.setBackground(BG); center.setBorder(new EmptyBorder(25, 34, 25, 34)); center.setLayout(new BorderLayout(0, 18));
        JPanel list = new JPanel(); list.setBackground(BG); list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        if (data.users.isEmpty()) {
            JPanel empty = card(); empty.setPreferredSize(new Dimension(0, 120));
            empty.add(label("No profiles yet — create the first SkillSwap profile!", 15, FontStyle.BOLD, TEXT), BorderLayout.CENTER);
            list.add(empty);
        } else {
            for (User u : data.users) list.add(profileChoiceCard(u));
        }
        center.add(scroll(list), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); bottom.setOpaque(false);
        JButton create = button("+  Create New Profile", PURPLE); create.addActionListener(e -> showCreateProfile());
        JButton back = outlineButton("Back"); back.addActionListener(e -> showWelcome());
        bottom.add(create); bottom.add(back); center.add(bottom, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER); setScreen(root);
    }

    JPanel profileChoiceCard(User user) {
        JPanel p = card(); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 104));
        JPanel avatar = new JPanel(new GridBagLayout()); avatar.setBackground(PURPLE_LIGHT); avatar.setPreferredSize(new Dimension(60,60));
        avatar.add(label(initials(user.name), 20, FontStyle.BOLD, PURPLE));
        p.add(avatar, BorderLayout.WEST);
        JPanel info = new JPanel(); info.setOpaque(false); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(label(user.name, 17, FontStyle.BOLD, NAVY));
        info.add(Box.createVerticalStrut(4)); info.add(label(user.email, 13, FontStyle.PLAIN, MUTED));
        info.add(Box.createVerticalStrut(7)); info.add(label("Teaches: " + String.join(", ", user.teachSkills), 12, FontStyle.PLAIN, TEXT));
        p.add(info, BorderLayout.CENTER);
        JButton login = button("Open Profile", PURPLE); login.setPreferredSize(new Dimension(125, 42)); login.addActionListener(e -> loginUser(user));
        p.add(login, BorderLayout.EAST);
        return p;
    }

    void loginUser(User user) { currentUser = user; showDashboard(); }

    // ============================================================
    // CREATE PROFILE
    // ============================================================
    void showCreateProfile() {
        JPanel root = page("Create Your Profile", "Tell the community what you can teach and what you want to learn.");
        JPanel content = new JPanel(new BorderLayout(0, 15)); content.setBackground(BG); content.setBorder(new EmptyBorder(25, 35, 25, 35)); root.add(content, BorderLayout.CENTER);

        JPanel form = card(); form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7,7,7,7); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        JTextField name = field(); JTextField email = field(); JTextField teach = field(); JTextField learn = field(); JTextField availability = field(); availability.setText("Mon-Fri 5PM-8PM");
        JComboBox<String> exp = new JComboBox<>(new String[]{"Beginner","Intermediate","Advanced","Expert"}); styleCombo(exp);
        JTextArea bio = new JTextArea(4, 20); styleArea(bio);
        addFormRow(form,g,0,"Full Name",name); addFormRow(form,g,1,"Email Address",email); addFormRow(form,g,2,"Skills I Can Teach",teach);
        addFormRow(form,g,3,"Skills I Want To Learn",learn); addFormRow(form,g,4,"Experience Level",exp); addFormRow(form,g,5,"Availability",availability);
        addFormRow(form,g,6,"Short Bio",new JScrollPane(bio));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT)); buttons.setOpaque(false);
        JButton back = outlineButton("Back"); JButton save = button("Create Profile  →", PURPLE);
        back.addActionListener(e -> showProfiles());
        save.addActionListener(e -> {
            String n=name.getText().trim(), em=email.getText().trim();
            if(n.isEmpty() || em.isEmpty()){ JOptionPane.showMessageDialog(this,"Please enter your name and email.","Missing Information",JOptionPane.WARNING_MESSAGE); return; }
            User u=new User(n,em,bio.getText(),exp.getSelectedItem().toString(),availability.getText(),parseSkills(teach.getText()),parseSkills(learn.getText()));
            data.users.add(u); currentUser=u; saveData(); showDashboard();
        });
        buttons.add(back); buttons.add(save); content.add(scroll(form),BorderLayout.CENTER); content.add(buttons,BorderLayout.SOUTH); setScreen(root);
    }

    void addFormRow(JPanel form, GridBagConstraints g, int row, String name, Component c) {
        g.gridy=row; g.gridx=0; g.weightx=0; g.gridwidth=1; form.add(label(name,13,FontStyle.BOLD,TEXT),g);
        g.gridx=1; g.weightx=1; g.gridwidth=2; form.add(c,g);
    }
    JTextField field(){ JTextField f=new JTextField(); styleField(f); return f; }
    List<String> parseSkills(String text){ List<String> list=new ArrayList<>(); for(String s:text.split(",")){s=s.trim();if(!s.isEmpty())list.add(s);} return list; }

    // ============================================================
    // DASHBOARD
    // ============================================================
    void showDashboard() {
        JPanel content = contentWithSidebar("Dashboard", "Your peer-learning space at a glance");
        JPanel body = new JPanel(new BorderLayout(0,18)); body.setOpaque(false);

        JPanel hero = new JPanel(new BorderLayout(15,10)); hero.setBackground(WHITE); hero.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(new Color(215,220,235),20),new EmptyBorder(20,24,20,24)));
        JPanel hleft=new JPanel();hleft.setOpaque(false);hleft.setLayout(new BoxLayout(hleft,BoxLayout.Y_AXIS));
        hleft.add(label("Welcome back, " + currentUser.name + "!", 26, FontStyle.BOLD, NAVY));
        hleft.add(Box.createVerticalStrut(5)); hleft.add(label("Keep learning, keep sharing, keep growing. ✦",14,FontStyle.PLAIN,MUTED));
        hero.add(hleft,BorderLayout.WEST);
        hero.add(pill("Community Member", PURPLE_LIGHT, PURPLE),BorderLayout.EAST);
        body.add(hero,BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1,4,14,14)); stats.setOpaque(false);
        stats.add(statCard("Skills to teach", String.valueOf(currentUser.teachSkills.size()), "Share your strengths", PURPLE, PURPLE_LIGHT));
        stats.add(statCard("Skills to learn", String.valueOf(currentUser.learnSkills.size()), "Keep exploring", BLUE, BLUE_LIGHT));
        stats.add(statCard("Completed", String.valueOf(currentUser.completedSessions), "Sessions finished", GREEN, GREEN_LIGHT));
        stats.add(statCard("Rating", currentUser.getRating()==0?"—":String.format("%.1f ★",currentUser.getRating()), "Peer feedback", ORANGE, ORANGE_LIGHT));
        body.add(stats,BorderLayout.CENTER);

        JPanel quick = card(); quick.setLayout(new BorderLayout(0,14));
        quick.add(label("Quick Actions",18,FontStyle.BOLD,NAVY),BorderLayout.NORTH);
        JPanel actions=new JPanel(new GridLayout(2,3,12,12));actions.setOpaque(false);
        actions.add(actionCard("Find Learning Partners","Discover peers who can teach you",PURPLE,()->showMatches()));
        actions.add(actionCard("Schedule a Session","Plan your next learning exchange",BLUE,()->showSchedule()));
        actions.add(actionCard("My Sessions","Manage requests and meetings",CYAN,()->showSessions()));
        actions.add(actionCard("My Profile","View your skills and bio",PINK,()->showProfile()));
        actions.add(actionCard("Notes & Resources","Share useful learning material",ORANGE,()->showNotes()));
        actions.add(actionCard("Ratings & Badges","See achievements and ratings",GREEN,()->showRatings()));
        quick.add(actions,BorderLayout.CENTER); body.add(quick,BorderLayout.SOUTH);
        content.add(scroll(body),BorderLayout.CENTER); setScreen(content.getParent() instanceof JPanel ? (JPanel)content.getParent() : content);
    }

    JPanel statCard(String title,String value,String sub,Color accent,Color light){
        JPanel p=card();p.setLayout(new BorderLayout(8,5));
        JPanel dot=new JPanel();dot.setBackground(accent);dot.setPreferredSize(new Dimension(6,45));
        JPanel text=new JPanel();text.setOpaque(false);text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));
        text.add(label(title,12,FontStyle.BOLD,MUTED));text.add(Box.createVerticalStrut(2));text.add(label(value,25,FontStyle.BOLD,NAVY));text.add(label(sub,11,FontStyle.PLAIN,MUTED));
        p.add(dot,BorderLayout.WEST);p.add(text,BorderLayout.CENTER);return p;
    }
    JButton actionCard(String title,String sub,Color color,Runnable r){
        JButton b=new JButton();b.setLayout(new BorderLayout(8,3));b.setBackground(WHITE);b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setBorder(new RoundBorder(BORDER,16));
        JPanel t=new JPanel();t.setOpaque(false);t.setLayout(new BoxLayout(t,BoxLayout.Y_AXIS));t.add(label(title,14,FontStyle.BOLD,NAVY));t.add(label(sub,11,FontStyle.PLAIN,MUTED));b.add(t,BorderLayout.CENTER);
        JPanel accent=new JPanel();accent.setBackground(color);accent.setPreferredSize(new Dimension(5,50));b.add(accent,BorderLayout.WEST);b.add(label("›",24,FontStyle.BOLD,color),BorderLayout.EAST);b.addActionListener(e->r.run());return b;
    }

    // ============================================================
    // PROFILE
    // ============================================================
    void showProfile(){
        JPanel content=contentWithSidebar("My Profile","Your SkillSwap identity and learning interests");
        JPanel body=new JPanel(new BorderLayout(18,18));body.setOpaque(false);
        JPanel profile=card(); profile.setPreferredSize(new Dimension(310,0));
        JPanel av=new JPanel(new GridBagLayout());av.setBackground(PURPLE_LIGHT);av.setPreferredSize(new Dimension(100,100));av.add(label(initials(currentUser.name),34,FontStyle.BOLD,PURPLE));
        profile.add(av,BorderLayout.NORTH);JPanel info=new JPanel();info.setOpaque(false);info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        info.add(Box.createVerticalStrut(15));info.add(label(currentUser.name,22,FontStyle.BOLD,NAVY));info.add(Box.createVerticalStrut(4));info.add(label(currentUser.email,13,FontStyle.PLAIN,MUTED));
        info.add(Box.createVerticalStrut(16));info.add(pill(currentUser.experience,PURPLE_LIGHT,PURPLE));info.add(Box.createVerticalStrut(12));info.add(label("Availability",11,FontStyle.BOLD,MUTED));info.add(label(currentUser.availability,13,FontStyle.PLAIN,TEXT));profile.add(info,BorderLayout.CENTER);
        body.add(profile,BorderLayout.WEST);

        JPanel details=new JPanel();details.setOpaque(false);details.setLayout(new BoxLayout(details,BoxLayout.Y_AXIS));
        JPanel skills=card();skills.setLayout(new GridLayout(1,2,20,0));skills.add(skillColumn("I CAN TEACH",currentUser.teachSkills,PURPLE));skills.add(skillColumn("I WANT TO LEARN",currentUser.learnSkills,BLUE));
        JPanel bio=card();bio.setLayout(new BoxLayout(bio,BoxLayout.Y_AXIS));bio.add(label("About me",17,FontStyle.BOLD,NAVY));bio.add(Box.createVerticalStrut(10));bio.add(label(currentUser.bio==null||currentUser.bio.trim().isEmpty()?"No bio added yet.":"<html>"+escape(currentUser.bio).replace("\n","<br>")+"</html>",13,FontStyle.PLAIN,TEXT));
        details.add(skills);details.add(Box.createVerticalStrut(15));details.add(bio);details.add(Box.createVerticalGlue());body.add(details,BorderLayout.CENTER);
        content.add(scroll(body),BorderLayout.CENTER);setScreen((JPanel)content.getParent());
    }

    JPanel skillColumn(String heading,List<String> skills,Color color){
        JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.add(label(heading,11,FontStyle.BOLD,MUTED));p.add(Box.createVerticalStrut(10));
        if(skills.isEmpty())p.add(label("None added",13,FontStyle.PLAIN,MUTED)); else for(String s:skills){JPanel row=pill(s,color==PURPLE?PURPLE_LIGHT:BLUE_LIGHT,color);row.setAlignmentX(Component.LEFT_ALIGNMENT);p.add(row);p.add(Box.createVerticalStrut(7));}return p;
    }
    String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}

    // ============================================================
    // MATCHMAKING
    // ============================================================
    void showMatches(){
        JPanel content=contentWithSidebar("Find Learning Partners","Search for people who can teach the skill you want to learn");
        JPanel body=new JPanel(new BorderLayout(0,15));body.setOpaque(false);
        JPanel searchCard=card();searchCard.setLayout(new BorderLayout(12,0));JTextField search=field();search.setPreferredSize(new Dimension(300,42));
        JButton find=button("Search Partners",PURPLE);searchCard.add(label("Skill",13,FontStyle.BOLD,TEXT),BorderLayout.WEST);searchCard.add(search,BorderLayout.CENTER);searchCard.add(find,BorderLayout.EAST);body.add(searchCard,BorderLayout.NORTH);
        JPanel results=new JPanel();results.setOpaque(false);results.setLayout(new BoxLayout(results,BoxLayout.Y_AXIS));body.add(scroll(results),BorderLayout.CENTER);
        find.addActionListener(e->populateMatches(search.getText(),results));
        content.add(body,BorderLayout.CENTER);setScreen((JPanel)content.getParent());
    }

    void populateMatches(String q,JPanel results){
        results.removeAll();q=q.trim().toLowerCase();int count=0;
        for(User u:data.users){if(u==currentUser)continue;boolean teaches=false,reciprocal=false;
            for(String s:u.teachSkills)if(q.isEmpty()||s.toLowerCase().contains(q)){teaches=true;break;}
            for(String a:currentUser.teachSkills)for(String w:u.learnSkills)if(a.equalsIgnoreCase(w)||a.toLowerCase().contains(w.toLowerCase())||w.toLowerCase().contains(a.toLowerCase()))reciprocal=true;
            if(teaches){results.add(matchCard(u,reciprocal));results.add(Box.createVerticalStrut(12));count++;}
        }
        if(count==0){JPanel empty=card();empty.add(label(q.isEmpty()?"Type a skill to find matching partners.":"No matching partner found for ‘"+q+"’. Try another skill.",14,FontStyle.PLAIN,MUTED),BorderLayout.CENTER);results.add(empty);}
        results.revalidate();results.repaint();
    }

    JPanel matchCard(User u,boolean reciprocal){
        JPanel p=card();
        JPanel av=new JPanel(new GridBagLayout());av.setBackground(BLUE_LIGHT);av.setPreferredSize(new Dimension(62,62));av.add(label(initials(u.name),20,FontStyle.BOLD,BLUE));p.add(av,BorderLayout.WEST);
        JPanel info=new JPanel();info.setOpaque(false);info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        JPanel nameRow=new JPanel(new FlowLayout(FlowLayout.LEFT,7,0));nameRow.setOpaque(false);nameRow.add(label(u.name,17,FontStyle.BOLD,NAVY));nameRow.add(pill(u.experience,PURPLE_LIGHT,PURPLE));if(reciprocal)nameRow.add(pill("Reciprocal match",GREEN_LIGHT,GREEN));info.add(nameRow);
        info.add(Box.createVerticalStrut(7));info.add(label("Teaches: "+String.join(", ",u.teachSkills),12,FontStyle.PLAIN,TEXT));info.add(Box.createVerticalStrut(4));info.add(label("Wants: "+String.join(", ",u.learnSkills),12,FontStyle.PLAIN,TEXT));info.add(Box.createVerticalStrut(4));info.add(label("Available: "+u.availability+"   •   Rating: "+(u.getRating()==0?"New":String.format("%.1f ★",u.getRating())),12,FontStyle.PLAIN,MUTED));p.add(info,BorderLayout.CENTER);
        JButton req=button("Request Session",PURPLE);req.setPreferredSize(new Dimension(150,44));req.addActionListener(e->showRequestDialog(u));p.add(req,BorderLayout.EAST);return p;
    }

    void showRequestDialog(User teacher){
        JPanel form=new JPanel(new GridLayout(3,2,10,10));form.setBorder(new EmptyBorder(8,8,8,8));
        JTextField skill=field(),date=field(),time=field();date.setText("30-09-2026");time.setText("6:00 PM");
        form.add(label("Skill",13,FontStyle.BOLD,TEXT));form.add(skill);form.add(label("Date",13,FontStyle.BOLD,TEXT));form.add(date);form.add(label("Time",13,FontStyle.BOLD,TEXT));form.add(time);
        int r=JOptionPane.showConfirmDialog(this,form,"Request session with "+teacher.name,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(r==JOptionPane.OK_OPTION){if(skill.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Please enter a skill.","Missing Skill",JOptionPane.WARNING_MESSAGE);return;}data.sessions.add(new Session(currentUser.name,teacher.name,skill.getText().trim(),date.getText().trim(),time.getText().trim()));saveData();JOptionPane.showMessageDialog(this,"Learning request sent to "+teacher.name+"!","Request Sent",JOptionPane.INFORMATION_MESSAGE);}
    }

    // ============================================================
    // SCHEDULE
    // ============================================================
    void showSchedule(){
        JPanel content=contentWithSidebar("Schedule a Session","Plan a learning exchange with another SkillSwap member");
        JPanel center=new JPanel(new GridBagLayout());center.setOpaque(false);GridBagConstraints g=new GridBagConstraints();g.fill=GridBagConstraints.HORIZONTAL;g.insets=new Insets(8,8,8,8);g.weightx=1;
        JPanel form=card();form.setLayout(new GridBagLayout());GridBagConstraints f=new GridBagConstraints();f.insets=new Insets(9,9,9,9);f.fill=GridBagConstraints.HORIZONTAL;f.weightx=1;
        JComboBox<String> teacher=new JComboBox<>();for(User u:data.users)if(u!=currentUser)teacher.addItem(u.name);styleCombo(teacher);JTextField skill=field(),date=field(),time=field();
        addFormRow(form,f,0,"Learning Partner",teacher);addFormRow(form,f,1,"Skill",skill);addFormRow(form,f,2,"Date",date);addFormRow(form,f,3,"Time",time);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));actions.setOpaque(false);JButton back=outlineButton("Back");JButton send=button("Send Request  →",BLUE);back.addActionListener(e->showDashboard());
        send.addActionListener(e->{if(teacher.getItemCount()==0){JOptionPane.showMessageDialog(this,"Create another user profile first.");return;}data.sessions.add(new Session(currentUser.name,teacher.getSelectedItem().toString(),skill.getText().trim(),date.getText().trim(),time.getText().trim()));saveData();JOptionPane.showMessageDialog(this,"Session request sent!","Success",JOptionPane.INFORMATION_MESSAGE);showDashboard();});actions.add(back);actions.add(send);
        f.gridy=4;f.gridx=0;f.gridwidth=2;form.add(actions,f);center.add(form);content.add(center,BorderLayout.CENTER);setScreen((JPanel)content.getParent());
    }

    // ============================================================
    // SESSIONS
    // ============================================================
    void showSessions(){
        JPanel content=contentWithSidebar("My Sessions","Track requests, accept meetings and mark completed exchanges");
        JPanel list=new JPanel();list.setOpaque(false);list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));boolean found=false;
        for(Session s:data.sessions){if(!s.learner.equals(currentUser.name)&&!s.teacher.equals(currentUser.name))continue;found=true;list.add(sessionCard(s));list.add(Box.createVerticalStrut(12));}
        if(!found){JPanel empty=card();empty.add(label("No sessions yet. Find a learning partner to get started!",14,FontStyle.PLAIN,MUTED),BorderLayout.CENTER);list.add(empty);}
        content.add(scroll(list),BorderLayout.CENTER);setScreen((JPanel)content.getParent());
    }

    JPanel sessionCard(Session s){
        JPanel p=card();String other=s.learner.equals(currentUser.name)?s.teacher:s.learner;
        JPanel icon=new JPanel(new GridBagLayout());icon.setBackground(statusColor(s.status, true));icon.setPreferredSize(new Dimension(64,64));icon.add(label("↔",24,FontStyle.BOLD,statusColor(s.status,false)));p.add(icon,BorderLayout.WEST);
        JPanel info=new JPanel();info.setOpaque(false);info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));row.setOpaque(false);row.add(label(s.skill,17,FontStyle.BOLD,NAVY));row.add(statusPill(s.status));info.add(row);
        info.add(Box.createVerticalStrut(7));info.add(label("Learning partner: "+other,12,FontStyle.PLAIN,TEXT));info.add(Box.createVerticalStrut(4));info.add(label("Date: "+s.date+"   •   Time: "+s.time,12,FontStyle.PLAIN,MUTED));p.add(info,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,7,7));actions.setOpaque(false);
        if(s.teacher.equals(currentUser.name)&&s.status.equals("Pending")){JButton a=button("Accept",GREEN);JButton d=outlineButton("Decline");a.addActionListener(e->{s.status="Accepted";saveData();showSessions();});d.addActionListener(e->{s.status="Declined";saveData();showSessions();});actions.add(a);actions.add(d);}
        if(s.status.equals("Accepted")){JButton c=button("Mark Completed",PURPLE);c.addActionListener(e->{s.status="Completed";for(User u:data.users)if(u.name.equals(s.learner)||u.name.equals(s.teacher))u.completedSessions++;saveData();showSessions();});actions.add(c);}
        p.add(actions,BorderLayout.EAST);return p;
    }

    Color statusColor(String status,boolean light){switch(status){case "Accepted":return light?GREEN_LIGHT:GREEN;case "Completed":return light?BLUE_LIGHT:BLUE;case "Declined":return light?new Color(255,235,237):RED;default:return light?ORANGE_LIGHT:ORANGE;}}
    JPanel statusPill(String s){return pill(s,statusColor(s,true),statusColor(s,false));}

    // ============================================================
    // NOTES
    // ============================================================
    void showNotes(){
        JPanel content=contentWithSidebar("Notes & Resources","Keep useful links and session notes in one place");
        JPanel outer=new JPanel(new BorderLayout(0,15));outer.setOpaque(false);
        JPanel head=new JPanel(new BorderLayout());head.setOpaque(false);head.add(label(data.notes.size()+" shared resource"+(data.notes.size()==1?"":"s"),13,FontStyle.BOLD,MUTED),BorderLayout.WEST);JButton add=button("+  Add Note / Resource",ORANGE);add.addActionListener(e->addNote());head.add(add,BorderLayout.EAST);outer.add(head,BorderLayout.NORTH);
        JPanel list=new JPanel();list.setOpaque(false);list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        if(data.notes.isEmpty()){JPanel empty=card();empty.add(label("No notes yet. Add your first learning resource!",14,FontStyle.PLAIN,MUTED),BorderLayout.CENTER);list.add(empty);}else for(Note n:data.notes){list.add(noteCard(n));list.add(Box.createVerticalStrut(12));}
        outer.add(scroll(list),BorderLayout.CENTER);content.add(outer,BorderLayout.CENTER);setScreen((JPanel)content.getParent());
    }

    JPanel noteCard(Note n){
        JPanel p=card();JPanel icon=new JPanel(new GridBagLayout());icon.setBackground(ORANGE_LIGHT);icon.setPreferredSize(new Dimension(55,55));icon.add(label("▤",23,FontStyle.BOLD,ORANGE));p.add(icon,BorderLayout.WEST);
        JPanel info=new JPanel();info.setOpaque(false);info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));info.add(label(n.title,17,FontStyle.BOLD,NAVY));info.add(Box.createVerticalStrut(5));info.add(label("By "+n.author+"  •  Partner: "+n.partner,11,FontStyle.BOLD,MUTED));info.add(Box.createVerticalStrut(8));info.add(label("<html>"+escape(n.content==null?"":n.content)+"</html>",12,FontStyle.PLAIN,TEXT));if(n.link!=null&&!n.link.trim().isEmpty()){info.add(Box.createVerticalStrut(7));info.add(label("Resource: "+escape(n.link),12,FontStyle.PLAIN,BLUE));}p.add(info,BorderLayout.CENTER);return p;
    }

    void addNote(){
        JPanel form=new JPanel(new GridLayout(4,2,10,10));form.setBorder(new EmptyBorder(8,8,8,8));JTextField partner=field(),title=field(),link=field();JTextArea content=new JTextArea(5,20);styleArea(content);JScrollPane sp=new JScrollPane(content);sp.setBorder(new RoundBorder(BORDER,12));
        form.add(label("Partner",13,FontStyle.BOLD,TEXT));form.add(partner);form.add(label("Title",13,FontStyle.BOLD,TEXT));form.add(title);form.add(label("Resource Link",13,FontStyle.BOLD,TEXT));form.add(link);form.add(label("Notes",13,FontStyle.BOLD,TEXT));form.add(sp);
        if(JOptionPane.showConfirmDialog(this,form,"Add Shared Note",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){data.notes.add(new Note(currentUser.name,partner.getText(),title.getText(),content.getText(),link.getText()));saveData();showNotes();}
    }

    // ============================================================
    // RATINGS & BADGES
    // ============================================================
    void showRatings(){
        JPanel content=contentWithSidebar("Ratings & Badges","Your progress, community reputation and achievements");
        JPanel body=new JPanel(new BorderLayout(18,18));body.setOpaque(false);
        JPanel summary=card();summary.setPreferredSize(new Dimension(300,0));summary.setLayout(new BoxLayout(summary,BoxLayout.Y_AXIS));
        summary.add(label("YOUR REPUTATION",11,FontStyle.BOLD,MUTED));summary.add(Box.createVerticalStrut(8));summary.add(label(currentUser.getRating()==0?"New":"★ "+String.format("%.1f",currentUser.getRating()),32,FontStyle.BOLD,ORANGE));summary.add(Box.createVerticalStrut(5));summary.add(label(currentUser.ratingCount+" peer rating"+(currentUser.ratingCount==1?"":"s"),12,FontStyle.PLAIN,MUTED));summary.add(Box.createVerticalStrut(20));summary.add(label("Completed Sessions",12,FontStyle.BOLD,MUTED));summary.add(Box.createVerticalStrut(4));summary.add(label(String.valueOf(currentUser.completedSessions),24,FontStyle.BOLD,NAVY));summary.add(Box.createVerticalGlue());JButton rate=button("Rate a Learning Partner",PURPLE);rate.addActionListener(e->ratePartner());summary.add(rate);body.add(summary,BorderLayout.WEST);
        JPanel right=new JPanel();right.setOpaque(false);right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS));right.add(label("Your Achievements",20,FontStyle.BOLD,NAVY));right.add(Box.createVerticalStrut(12));
        for(String b:getBadges())right.add(badgeCard(b));right.add(Box.createVerticalGlue());body.add(scroll(right),BorderLayout.CENTER);
        content.add(body,BorderLayout.CENTER);setScreen((JPanel)content.getParent());
    }

    JPanel badgeCard(String badge){Color c=badgeColor(badge);JPanel p=card();p.setMaximumSize(new Dimension(Integer.MAX_VALUE,88));JPanel icon=new JPanel(new GridBagLayout());icon.setBackground(c);icon.setPreferredSize(new Dimension(55,55));icon.add(label("★",22,FontStyle.BOLD,WHITE));p.add(icon,BorderLayout.WEST);JPanel t=new JPanel();t.setOpaque(false);t.setLayout(new BoxLayout(t,BoxLayout.Y_AXIS));t.add(label(badge,16,FontStyle.BOLD,NAVY));t.add(Box.createVerticalStrut(4));t.add(label(badgeDescription(badge),12,FontStyle.PLAIN,MUTED));p.add(t,BorderLayout.CENTER);return p;}
    Color badgeColor(String b){if(b.contains("Champion"))return PURPLE;if(b.contains("Mentor"))return PINK;if(b.contains("Learner"))return BLUE;if(b.contains("Community"))return GREEN;return ORANGE;}
    String badgeDescription(String b){if(b.equals("Knowledge Champion"))return "Completed 10 or more learning sessions";if(b.equals("Skill Mentor"))return "Completed 5 or more learning sessions";if(b.equals("Active Learner"))return "Completed 2 or more learning sessions";if(b.equals("Community Member"))return "Received your first peer rating";return "Start learning and sharing to unlock badges";}

    void ratePartner(){
        List<User> partners=new ArrayList<>();for(Session s:data.sessions){if(!s.status.equals("Completed"))continue;if(s.learner.equals(currentUser.name))for(User u:data.users)if(u.name.equals(s.teacher))partners.add(u);if(s.teacher.equals(currentUser.name))for(User u:data.users)if(u.name.equals(s.learner))partners.add(u);}
        if(partners.isEmpty()){JOptionPane.showMessageDialog(this,"You need a completed session before rating someone.","No Completed Session",JOptionPane.INFORMATION_MESSAGE);return;}
        JComboBox<String> partner=new JComboBox<>();for(User u:partners)partner.addItem(u.name);styleCombo(partner);JComboBox<Integer> stars=new JComboBox<>(new Integer[]{1,2,3,4,5});styleCombo(stars);
        JPanel form=new JPanel(new GridLayout(2,2,10,10));form.setBorder(new EmptyBorder(8,8,8,8));form.add(label("Partner",13,FontStyle.BOLD,TEXT));form.add(partner);form.add(label("Rating",13,FontStyle.BOLD,TEXT));form.add(stars);
        if(JOptionPane.showConfirmDialog(this,form,"Rate Learning Partner",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){String n=partner.getSelectedItem().toString();int v=(Integer)stars.getSelectedItem();for(User u:data.users)if(u.name.equals(n)){double total=u.rating*u.ratingCount;u.ratingCount++;u.rating=(total+v)/u.ratingCount;}saveData();JOptionPane.showMessageDialog(this,"Rating submitted! Thank you for helping the community.");showRatings();}
    }

    List<String> getBadges(){List<String> b=new ArrayList<>();if(currentUser.completedSessions>=10)b.add("Knowledge Champion");if(currentUser.completedSessions>=5)b.add("Skill Mentor");if(currentUser.completedSessions>=2)b.add("Active Learner");if(currentUser.ratingCount>0)b.add("Community Member");if(b.isEmpty())b.add("Beginner");return b;}
}
