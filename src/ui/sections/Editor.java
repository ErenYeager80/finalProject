package ui.sections;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.collection.ListModification;

import java.util.Collection;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;


public class Editor extends CodeArea {


    private static class VisibleParagraphStyler<PS, SEG, S> implements Consumer<ListModification<? extends Paragraph<PS, SEG, S>>> {
        private final GenericStyledArea<PS, SEG, S> area;
        private final Function<String, StyleSpans<S>> computeStyles;
        private int prevParagraph, prevTextLength;

        public VisibleParagraphStyler( GenericStyledArea<PS, SEG, S> area, Function<String,StyleSpans<S>> computeStyles )
        {
            this.computeStyles = computeStyles;
            this.area = area;
        }

        @Override
        public void accept( ListModification<? extends Paragraph<PS, SEG, S>> lm )
        {
            if ( lm.getAddedSize() > 0 )
            {
                int paragraph = Math.min( area.firstVisibleParToAllParIndex() + lm.getFrom(), area.getParagraphs().size()-1 );
                String text = area.getText( paragraph, 0, paragraph, area.getParagraphLength( paragraph ) );

                if ( paragraph != prevParagraph || text.length() != prevTextLength )
                {
                    int startPos = area.getAbsolutePosition( paragraph, 0 );
                    Platform.runLater( () -> area.setStyleSpans( startPos, computeStyles.apply( text ) ) );
                    prevTextLength = text.length();
                    prevParagraph = paragraph;
                }
            }
        }
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {

//        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
//        ExpressionPattern ep =Pattern.matchType(text);
//        if(ep!=null&&ep.type()==null)
//            spansBuilder.add(Collections.singleton("error"), text.length());
//        else {
//            Matcher matcher = getPATTERN().matcher(text);
//            while (matcher.find()) {
//                String styleClass =
//                        matcher.group("KEYWORD") != null ? "keyword" :
//                                matcher.group("PAREN") != null ? "paren" :
//                                        matcher.group("BRACE") != null ? "brace" :
//                                                matcher.group("BRACKET") != null ? "bracket" :
//                                                        matcher.group("SEMICOLON") != null ? "semicolon" :
//                                                                matcher.group("STRING") != null ? "string" :
//                                                                        matcher.group("COMMENT") != null ? "comment" : null;
//
//                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
//                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
//                lastKwEnd = matcher.end();
//            }
//            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
//        }

        return spansBuilder.create();
    }


    private static class DefaultContextMenu extends ContextMenu {

        public DefaultContextMenu()
        {
            MenuItem fold = new MenuItem("Fold selected text");
            fold.setOnAction(AE -> { hide(); fold(); } );

            MenuItem unfold = new MenuItem("Unfold from cursor");
            unfold.setOnAction(AE -> { hide(); unfold(); } );

            MenuItem print = new MenuItem("Print");
            print.setOnAction(AE -> { hide(); print(); } );

            getItems().addAll(fold, unfold, print);
        }

        /**
         * Folds multiple lines of selected text, only showing the first line and hiding the rest.
         */
        private void fold() {
            ((CodeArea) getOwnerNode()).foldSelectedParagraphs();
        }

        /**
         * Unfold the CURRENT line/paragraph if it has a fold.
         */
        private void unfold() {
            CodeArea area = (CodeArea) getOwnerNode();
            area.unfoldParagraphs( area.getCurrentParagraph() );
        }

        private void print() {
            System.out.println( ((CodeArea) getOwnerNode()).getText() );
        }
    }

    public Editor(){
        setParagraphGraphicFactory(LineNumberFactory.get(this));
        setContextMenu(new DefaultContextMenu());

        getVisibleParagraphs().addModificationObserver(new VisibleParagraphStyler<>(this, this::computeHighlighting));


        final java.util.regex.Pattern whiteSpace = java.util.regex.Pattern.compile( "^\\s+" );
        addEventHandler( KeyEvent.KEY_PRESSED, KE ->
        {
            if ( KE.getCode() == KeyCode.ENTER ) {
                int caretPosition =getCaretPosition();
                int currentParagraph = getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher( getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
                if ( m0.find() ) Platform.runLater( () -> insertText( caretPosition, m0.group() ) );
            }
        });
        setText("\n");
    }
    public Editor(String text){
        this();
        setText(text);
    }
    public void setText(String text){
        this.replaceText(0, 0, text);
    }
}