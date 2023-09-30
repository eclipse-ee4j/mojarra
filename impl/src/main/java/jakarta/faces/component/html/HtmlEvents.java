package jakarta.faces.component.html;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.faces.context.FacesContext;

/**
 * https://html.spec.whatwg.org/multipage/webappapis.html#event-handlers-on-elements,-document-objects,-and-window-objects
 * 
 * 
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

    private static final Collection<String> DOCUMENT_ELEMENT_EVENT_NAMES = Arrays.stream(DocumentElementEvent.values()).map(e -> e.name()).sorted().distinct().toList();
    private static final Collection<String> BODY_ELEMENT_EVENT_NAMES = Stream.concat(DOCUMENT_ELEMENT_EVENT_NAMES.stream(), Arrays.stream(BodyElementEvent.values()).map(e -> e.name())).sorted().distinct().toList();

    private HtmlEvents() {
        throw new AssertionError();
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for document elements.
     */
    public static Collection<String> getDocumentElementEventNames(FacesContext context) {
        return mergeAdditionalHtmlEventNamesIfNecessary(context, "jakarta.faces.component.html.HtmlEvents.DOCUMENT_ELEMENT_EVENT_NAMES", DOCUMENT_ELEMENT_EVENT_NAMES, Collections.emptyList());
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for body elements.
     */
    public static Collection<String> getBodyElementEventNames(FacesContext context) {
        return mergeAdditionalHtmlEventNamesIfNecessary(context, "jakarta.faces.component.html.HtmlEvents.BODY_ELEMENT_EVENT_NAMES", DOCUMENT_ELEMENT_EVENT_NAMES, BODY_ELEMENT_EVENT_NAMES);
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> mergeAdditionalHtmlEventNamesIfNecessary(FacesContext context, String applicationKey, Collection<String> documentEventNames, Collection<String> bodyEventNames) {
        return (Collection<String>) context.getExternalContext().getApplicationMap().computeIfAbsent(applicationKey, $ -> {
            String[] additionalHtmlEventNames = Optional.ofNullable(context.getExternalContext().getInitParameter(ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME)).map(p -> p.split("\\s+")).orElse(new String[0]);
            return Stream.concat(Stream.concat(documentEventNames.stream(), bodyEventNames.stream()), Arrays.stream(additionalHtmlEventNames)).sorted().distinct().toList();
        });
    }

}
