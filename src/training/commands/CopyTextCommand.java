package training.commands;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.undo.BasicUndoableAction;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.DocumentReferenceManager;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.EditorGutter;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.DocumentUtil;
import org.jdom.Element;

/**
 * Created by karashevich on 30/01/15.
 */
public class CopyTextCommand extends Command {

    CopyTextCommand() {
        super(CommandType.COPYTEXT);
    }

    @Override
    public void execute(final ExecutionList executionList) throws InterruptedException {

        Element element = executionList.getElements().poll();
//        updateDescription(element, infoPanel, editor);

        final String finalText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        DocumentUtil.writeInRunUndoTransparentAction(() -> {
            final DocumentReference documentReference = DocumentReferenceManager.getInstance().create(executionList.getEditor().getDocument());
            UndoManager.getInstance(executionList.getProject()).nonundoableActionPerformed(documentReference, false);
            executionList.getEditor().getDocument().insertString(0, finalText);
        });

        PsiDocumentManager.getInstance(executionList.getProject()).commitDocument(executionList.getEditor().getDocument());

        CommandProcessor.getInstance().executeCommand(executionList.getProject(), () -> {UndoManager.getInstance(executionList.getProject()).undoableActionPerformed(new BasicUndoableAction() {
            @Override
            public void undo() {
            }

            @Override
            public void redo() {
            }
        });}, null, null);
        updateGutter(executionList);

        startNextCommand(executionList);
    }

    private void updateGutter(ExecutionList executionList) {
        final EditorGutter editorGutter = executionList.getEditor().getGutter();
        EditorGutterComponentEx editorGutterComponentEx = (EditorGutterComponentEx) editorGutter;
        editorGutterComponentEx.revalidateMarkup();
    }
}
