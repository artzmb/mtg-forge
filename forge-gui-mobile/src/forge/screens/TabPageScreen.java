package forge.screens;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.Forge;
import forge.Graphics;
import forge.assets.FImage;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinColor.Colors;
import forge.model.FModel;
import forge.properties.ForgePreferences.FPref;
import forge.toolbox.FContainer;
import forge.toolbox.FDisplayObject;
import forge.toolbox.FEvent;
import forge.toolbox.FLabel;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FScrollPane;
import forge.util.Utils;

public class TabPageScreen<T extends TabPageScreen<T>> extends FScreen {
    public static boolean COMPACT_TABS = FModel.getPreferences().getPrefBoolean(FPref.UI_COMPACT_TABS);

    protected final TabPage<T>[] tabPages;
    private TabPage<T> selectedPage;
    protected final TabHeader<T> tabHeader;

    @SuppressWarnings("unchecked")
    public TabPageScreen(TabPage<T>... tabPages0) {
        this(tabPages0, true);
    }

    @SuppressWarnings("unchecked")
    public TabPageScreen(TabPage<T>[] tabPages0, boolean showBackButton) {
        super(new TabHeader<T>(tabPages0, showBackButton));
        tabHeader = (TabHeader<T>)getHeader(); //cache reference to tab header with proper type

        int index = 0;
        tabPages = tabPages0;
        for (TabPage<T> tabPage : tabPages) {
            tabPage.index = index++;
            tabPage.parentScreen = (T)this;
            add(tabPage);
            tabPage.setVisible(false);
        }
        setSelectedPage(tabPages[0]);
    }

    public TabPage<T> getSelectedPage() {
        return selectedPage;
    }
    public void setSelectedPage(TabPage<T> tabPage0) {
        if (selectedPage == tabPage0) { return; }

        if (selectedPage != null) {
            selectedPage.setVisible(false);
        }
        selectedPage = tabPage0;
        if (selectedPage != null) {
            selectedPage.setVisible(true);
            if (canActivateTabPage()) {
                scrollSelectedTabIntoView();
                selectedPage.onActivate();
            }
        }
    }

    protected boolean canActivateTabPage() {
        return Forge.getCurrentScreen() == this;
    }

    @Override
    public void onActivate() {
        if (selectedPage != null) {
            scrollSelectedTabIntoView(); //ensure selected tab in view when screen activated
            selectedPage.onActivate();
        }
    }

    private void scrollSelectedTabIntoView() {
        if (tabHeader.isScrollable) { //scroll tab into view if needed, leaving half of the previous/next tab visible if possible
            tabHeader.scroller.scrollIntoView(selectedPage.tab, selectedPage.tab.getWidth() / 2);
        }
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        height -= startY;
        for (TabPage<T> tabPage : tabPages) {
            tabPage.setBounds(0, startY, width, height);
        }
    }

    private static class TabHeader<T extends TabPageScreen<T>> extends Header {
        private static final float HEIGHT = Math.round(Utils.AVG_FINGER_HEIGHT * 1.4f);
        private static final float COMPACT_HEIGHT = Math.round(Utils.AVG_FINGER_HEIGHT * 0.8f);
        private static final float BACK_BUTTON_WIDTH = Math.round(HEIGHT / 2);
        private static final FSkinColor SEPARATOR_COLOR = BACK_COLOR.stepColor(-40);

        private final FLabel btnBack;
        private boolean isScrollable;
        private FDisplayObject finalVisibleTab;

        private final FScrollPane scroller = add(new FScrollPane() {
            @Override
            protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
                int tabCount = 0;
                for (FDisplayObject child : getChildren()) {
                    if (child.isVisible()) {
                        tabCount++;
                        finalVisibleTab = child;
                    }
                }
                float x = 0;
                float tabWidth;
                isScrollable = (tabCount > 3); //support up to 3 tabs without scrolling
                if (isScrollable) {
                    tabWidth = visibleWidth / 2.5f; //support half of the third tab to make scrolling more obvious
                }
                else {
                    tabWidth = visibleWidth / tabCount;
                }
                for (FDisplayObject child : getChildren()) {
                    if (child.isVisible()) {
                        child.setBounds(x, 0, tabWidth, visibleHeight);
                        x += tabWidth;
                    }
                }
                return new ScrollBounds(isScrollable ? x : visibleWidth, visibleHeight);
            }
        });

        public TabHeader(TabPage<T>[] tabPages, boolean showBackButton) {
            if (showBackButton) {
                btnBack = add(new FLabel.Builder().icon(new BackIcon(BACK_BUTTON_WIDTH, BACK_BUTTON_WIDTH)).pressedColor(BTN_PRESSED_COLOR).align(HAlignment.CENTER).command(new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        Forge.back();
                    }
                }).build());
            }
            else {
                btnBack = null;
            }

            for (TabPage<T> tabPage : tabPages) {
                scroller.add(tabPage.tab);
            }
        }

        @Override
        public float getPreferredHeight() {
            return COMPACT_TABS ? COMPACT_HEIGHT : HEIGHT;
        }

        @Override
        public void drawBackground(Graphics g) {
            g.fillRect(BACK_COLOR, 0, 0, getWidth(), getHeight());
        }

        @Override
        public void drawOverlay(Graphics g) {
            //draw right border for back button
            if (btnBack != null) {
                float x = btnBack.getWidth() - LINE_THICKNESS / 2;
                g.drawLine(LINE_THICKNESS, SEPARATOR_COLOR, x, 0, x, getHeight());
            }

            //draw bottom border for header
            float y = getHeight() - LINE_THICKNESS / 2;
            g.drawLine(LINE_THICKNESS, LINE_COLOR, 0, y, getWidth(), y);
        }

        @Override
        protected void doLayout(float width, float height) {
            float x = 0;
            if (btnBack != null) {
                btnBack.setIconScaleAuto(COMPACT_TABS);
                btnBack.setSize(BACK_BUTTON_WIDTH, height);
                x += BACK_BUTTON_WIDTH;
            }
            scroller.setBounds(x, 0, width - x, height);
        }
    }

    public static abstract class TabPage<T extends TabPageScreen<T>> extends FContainer {
        private static final FSkinColor SEL_TAB_COLOR = FSkinColor.get(Colors.CLR_ACTIVE);
        private static final FSkinColor TAB_FORE_COLOR = FSkinColor.get(Colors.CLR_TEXT);
        private static final FSkinFont TAB_FONT = FSkinFont.get(12);

        protected T parentScreen;
        protected String caption;
        protected FImage icon;
        private int index;
        private final Tab tab;

        protected TabPage(String caption0, FImage icon0) {
            caption = caption0;
            icon = icon0;
            tab = new Tab();
        }

        public void showTab() {
            tab.setVisible(true);
        }
        public void hideTab() {
            tab.setVisible(false);
        }

        public String getCaption() {
            return caption;
        }

        public FImage getIcon() {
            return icon;
        }

        protected void onActivate() {
        }

        @Override
        public boolean fling(float velocityX, float velocityY) {
            //switch to next/previous tab page when flung left or right
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                if (velocityX < 0) {
                    if (index < parentScreen.tabPages.length - 1) {
                        parentScreen.setSelectedPage(parentScreen.tabPages[index + 1]);
                    }
                }
                else if (index > 0) {
                    parentScreen.setSelectedPage(parentScreen.tabPages[index - 1]);
                }
                return true;
            }
            return false;
        }

        protected class Tab extends FDisplayObject {
            @Override
            public boolean tap(float x, float y, int count) {
                parentScreen.setSelectedPage(TabPage.this);
                return true;
            }

            public void setVisible(boolean b0) {
                if (isVisible() == b0) { return; }
                super.setVisible(b0);
                parentScreen.getHeader().revalidate(); //revalidate header to account for tab visiblility change

                if (!b0 && parentScreen.getSelectedPage() == TabPage.this) {
                    //select next page if this page is hidden
                    for (int i = index + 1; i < parentScreen.tabPages.length; i++) {
                        if (parentScreen.tabPages[i].tab.isVisible()) {
                            parentScreen.setSelectedPage(parentScreen.tabPages[i]);
                            return;
                        }
                    }
                    //select previous page if selecting next page is not possible
                    for (int i = index - 1; i >= 0; i--) {
                        if (parentScreen.tabPages[i].tab.isVisible()) {
                            parentScreen.setSelectedPage(parentScreen.tabPages[i]);
                            return;
                        }
                    }
                    parentScreen.setSelectedPage(null);
                }
            }

            @Override
            public void draw(Graphics g) {
                float w = getWidth();
                float h = getHeight();
                float padding = h * 0.1f;
                if (parentScreen.getSelectedPage() == TabPage.this) {
                    g.fillRect(SEL_TAB_COLOR, Header.LINE_THICKNESS / 2, 0, w - Header.LINE_THICKNESS, h);
                }
                w -= 2 * padding;

                //draw caption and icon
                if (COMPACT_TABS) {
                    h -= 2 * padding;
                    if (icon == null) {
                        g.drawText(caption, TAB_FONT, TAB_FORE_COLOR, padding, padding, w, h, false, HAlignment.CENTER, true);
                    }
                    else {
                        //center combination of icon and text
                        float iconWidth = h * icon.getWidth() / icon.getHeight();
                        float iconOffset = iconWidth + padding;

                        float x = padding;
                        float y = padding;
                        float dx;
                        FSkinFont font = TAB_FONT;
                        while (true) {
                            dx = (w - iconOffset - font.getMultiLineBounds(caption).width) / 2;
                            if (dx > 0) {
                                x += dx;
                                break;
                            }
                            if (!font.canShrink()) {
                                break;
                            }
                            font = font.shrink();
                        }

                        g.drawImage(icon, x, y, iconWidth, h);

                        x += iconOffset;
                        w -= iconOffset;
                        g.startClip(x, y, w, h);
                        g.drawText(caption, font, TAB_FORE_COLOR, x, y, w, h, false, HAlignment.LEFT, true);
                        g.endClip();
                    }
                }
                else {
                    float y = h - padding - TAB_FONT.getCapHeight();
                    g.drawText(caption, TAB_FONT, TAB_FORE_COLOR, padding, y - padding, w, h - y + padding, false, HAlignment.CENTER, true);

                    if (icon != null) {
                        float iconHeight = y - 2 * padding;
                        float iconWidth = iconHeight * icon.getWidth() / icon.getHeight();
                        if (iconWidth > w) {
                            iconHeight *= w / iconWidth;
                            iconWidth = w;
                        }
                        g.drawImage(icon, padding + (w - iconWidth) / 2, (y - iconHeight) / 2, iconWidth, iconHeight);
                    }
                }

                //draw right border if needed
                if (parentScreen.tabHeader.finalVisibleTab != this) {
                    float x = getWidth() - Header.LINE_THICKNESS / 2;
                    g.drawLine(Header.LINE_THICKNESS, TabHeader.SEPARATOR_COLOR, x, 0, x, getHeight());
                }
            }
        }
    }
}
