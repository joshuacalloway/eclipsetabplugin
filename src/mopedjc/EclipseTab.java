package mopedjc;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Created by jc on 10/26/15.
 */
public class EclipseTab implements ModuleComponent {
    public EclipseTab(Module module) {
    }

    public void initComponent() {
        // insert component initialization logic here
    }

    public void disposeComponent() {
        // insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "Eclipse Tab Plugin";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public void moduleAdded() {
        EditorActionManager manager = EditorActionManager.getInstance();
        EditorActionHandler originalTabHandler = manager.getActionHandler(IdeActions.ACTION_EDITOR_TAB);
        manager.setActionHandler(IdeActions.ACTION_EDITOR_TAB, new EclipseTabHandler(originalTabHandler));
        TypedAction typedAction = manager.getTypedAction();
        typedAction.setupHandler(new DetectTriggerTypedKeysAction(typedAction.getHandler()));
    }

    boolean triggerKeysTyped = false;

    private class EclipseTabHandler extends EditorActionHandler {
        private final EditorActionHandler originalTabHandler;

        public EclipseTabHandler(EditorActionHandler originalTabHandler) {
            this.originalTabHandler = originalTabHandler;
        }

        @Override
        protected void doExecute(Editor editor, @Nullable Caret caret, DataContext dataContext) {
            System.out.println("MyTabActionHandler entry");

            PsiElement elementOnCursor = PsiUtilBase.getElementAtCaret(editor);
            char charOnCursor = elementOnCursor.textToCharArray()[0];

            if (triggerKeysTyped && Arrays.binarySearch(CLOSING_CHARS, charOnCursor) >= 0) {
                int textOffset = elementOnCursor.getTextOffset();
                editor.getCaretModel().moveToOffset(textOffset + elementOnCursor.getText().length());
                PsiElement newElementOnCursor = PsiUtilBase.getElementAtCaret(editor);
                charOnCursor = newElementOnCursor.textToCharArray()[0];

                if (Arrays.binarySearch(CLOSING_CHARS, charOnCursor) < 0) {
                    triggerKeysTyped = false;
                }
            } else {

                this.originalTabHandler.execute(editor, caret, dataContext);
            }
        }
    }
    private final char[] TRIGGER_CHARS = {'"', '\'', '(', '[', '`', '{'};
    private final char[] CLOSING_CHARS = {'"', '\'',')', ']', '}'};
    private class DetectTriggerTypedKeysAction implements TypedActionHandler {

        private final TypedActionHandler defaultHandler;

        public DetectTriggerTypedKeysAction(TypedActionHandler handler) {
            defaultHandler = handler;
        }

        @Override
        public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
            Document document = editor.getDocument();

            if (document.isWritable() &&
                    (Arrays.binarySearch(TRIGGER_CHARS, charTyped) >= 0)) {
                triggerKeysTyped = true;
            }

            defaultHandler.execute(editor, charTyped, dataContext);

        }
    }
}
