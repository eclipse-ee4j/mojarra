package jakarta.faces.component;

/**
 * <p class="changed_added_4_0">
 * <strong>Doctype</strong> is an interface that must be implemented by any {@link UIComponent} that represents a document type declaration.
 * </p>
 *
 * @since 4.0
 */
public interface Doctype {

    /**
     * Returns the name of the first element in the document, never <code>null</code>.
     * For example, <code>"html"</code>.
     * @return The name of the first element in the document, never <code>null</code>.
     */
    String getRootElement();

    /**
     * Returns the public identifier of the document, or <code>null</code> if there is none.
     * For example, <code>"-//W3C//DTD XHTML 1.1//EN"</code>.
     * @return The public identifier of the document, or <code>null</code> if there is none.
     */
    String getPublic();

    /**
     * Returns the system identifier of the document, or <code>null</code> if there is none.
     * For example, <code>"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"</code>.
     * @return The system identifier of the document, or <code>null</code> if there is none.
     */
    String getSystem();

}
