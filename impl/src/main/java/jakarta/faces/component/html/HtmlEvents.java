package jakarta.faces.component.html;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.BehaviorEvent.FacesComponentEvent;

/**
 * <p class="changed_added_5_0">
 * Events supported by HTML elements as per <a href="https://html.spec.whatwg.org/multipage/webappapis.html#event-handlers-on-elements,-document-objects,-and-window-objects">current spec</a>.
 * </p>
 * 
 * @since 5.0
 */
public final class HtmlEvents {

    /**
     * Events supported by all document elements.
     */
    public enum DocumentElementEvent {
        abort,
        auxclick,
        beforeinput,
        beforematch,
        eforetoggle,
        cancel,
        canplay,
        canplaythrough,
        change,
        click,
        close,
        contextlost,
        contextmenu,
        contextrestored,
        copy,
        cuechange,
        cut,
        dblclick,
        drag,
        dragend,
        dragenter,
        dragleave,
        dragover,
        dragstart,
        drop,
        durationchange,
        emptied,
        ended,
        formdata,
        input,
        invalid,
        keydown,
        keypress,
        keyup,
        loadeddata,
        loadedmetadata,
        loadstart,
        mousedown,
        mouseenter,
        mouseleave,
        mousemove,
        mouseout,
        mouseover,
        mouseup,
        paste,
        pause,
        play,
        playing,
        progress,
        ratechange,
        reset,
        securitypolicyviolation,
        seeked,
        seeking,
        select,
        slotchange,
        stalled,
        submit,
        suspend,
        timeupdate,
        toggle,
        volumechange,
        waiting,
        wheel,
        
    }

    /**
     * Events supported by all body elements, in addition to the events supported by all document elements.
     */
    public enum BodyElementEvent {
        blur,
        error,
        focus,
        load,
        resize,
        scroll,
        scrollend;
    }

    /**
     * The name of the context-param whose value must represent a space-separated list of additional HTML event names.
     * All supported HTML event names are defined in the enums {@link DocumentElementEvent} and {@link BodyElementEvent}.
     * Any HTML event name which you wish to add to these enums can be supplied via this context-param.
     */
    public static final String ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME = "jakarta.faces.ADDITIONAL_HTML_EVENT_NAMES";

    private HtmlEvents() {
        throw new AssertionError();
    }

    /**
     * @param context The involved faces context.
     * @return All additional HTML event names specified via {@link #ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME}.
     */
    public static Collection<String> getAdditionalHtmlEventNames(FacesContext context) {
        return collect(Arrays.stream(Optional.ofNullable(context.getExternalContext().getInitParameter(ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME)).map(p -> p.split("\\s+")).orElse(new String[0])));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML document elements, including additional HTML event names.
     */
    public static Collection<String> getHtmlDocumentElementEventNames(FacesContext context) {
        return cache(context, "HtmlEvents.DOCUMENT_ELEMENT_EVENT_NAMES", () -> merge(DocumentElementEvent.values(), getAdditionalHtmlEventNames(context)));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML body elements, including HTML document element event names.
     */
    public static Collection<String> getHtmlBodyElementEventNames(FacesContext context) {
        return cache(context, "HtmlEvents.BODY_ELEMENT_EVENT_NAMES", () -> merge(BodyElementEvent.values(), getHtmlDocumentElementEventNames(context)));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML implementations of Faces {@link ActionSource} components, including HTML body element event names.
     */
    public static Collection<String> getFacesActionSourceEventNames(FacesContext context) {
        return cache(context, "HtmlEvents.FACES_ACTION_SOURCE_EVENT_NAMES", () -> merge(FacesComponentEvent.action, getHtmlBodyElementEventNames(context)));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML implementations of Faces {@link EditableValueHolder} components, including HTML body element event names.
     */
    public static Collection<String> getFacesEditableValueHolderEventNames(FacesContext context) {
        return cache(context, "HtmlEvents.FACES_EDITABLE_VALUE_HOLDER_EVENT_NAMES", () -> merge(FacesComponentEvent.valueChange, getHtmlBodyElementEventNames(context)));
    }

    private static Collection<String> collect(Stream<String> stream) {
        return stream.sorted().distinct().collect(toUnmodifiableList());
    }

    private static Collection<String> merge(Enum<?> enumValue, Collection<String> eventNames) {
        return merge(new Enum[] { enumValue }, eventNames);
    }

    private static Collection<String> merge(Enum<?>[] enumValues, Collection<String> eventNames) {
        return collect(Stream.concat(Arrays.stream(enumValues).map(e -> e.name()), eventNames.stream()));
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> cache(FacesContext context, String applicationKey, Supplier<Collection<String>> supplier) {
        return (Collection<String>) context.getExternalContext().getApplicationMap().computeIfAbsent(applicationKey, $ -> supplier.get());
    }

}
