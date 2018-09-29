
/**
 * <p class="changed_added_2_0"><span class="changed_modified_2_0_rev_a
 * changed_modified_2_1 changed_modified_2_2 changed_modified_2_3">This</span>
 * package contains public classes for the Java code API of Facelets. The vast
 * majority of Facelets users have no need to access the Java API and can get
 * all their work done using the tag-level API. These classes are provided for
 * users that have a need for a Java API that allows participation in the
 * execution of a Facelets View, which happens as a result of the runtime
 * calling <code>{@link javax.faces.view.ViewDeclarationLanguage#buildView(javax.faces.context.FacesContext, javax.faces.component.UIViewRoot)}</code>.
 * </p>
 * <div class="changed_added_2_0">
 * <p>
 * <img src="doc-files/Facelets.jpg" alt="UML Class Diagram of classes in this package">
 * </p>
 * <p>
 * The most common usecase for participating in the execution of a Facelets View
 * is to provide a custom tag handler in those cases when the non-Java API
 * methods for doing so is not sufficient. In such cases, Java classes may
 * extend from <code>{@link javax.faces.view.facelets.ComponentHandler}</code>, <code>{@link javax.faces.view.facelets.BehaviorHandler}</code>, <code>
 * {@link javax.faces.view.facelets.ConverterHandler}</code>, or <code>{@link javax.faces.view.facelets.ValidatorHandler}</code> depending upon the
 * kind of JSF Java API artifact they want to represent in the Facelets VDL
 * page.</p>
 *
 * </div>
 */
package javax.faces.view.facelets;
