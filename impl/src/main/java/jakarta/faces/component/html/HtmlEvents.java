package jakarta.faces.component.html;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.BehaviorEvent.FacesComponentEvent;

/**
 * <p class="changed_added_5_0">
 * Events supported by HTML elements as per <a href="https://html.spec.whatwg.org/multipage/webappapis.html#event-handlers-on-elements,-document-objects,-and-window-objects">current spec</a>.
 * These can be used to supply {@link ClientBehaviorHolder#getEventNames()} and {@link ClientBehaviorHolder#getDefaultEventName()}.
 * </p>
 *
 * @since 5.0
 */
public final class HtmlEvents {

    /**
     * Events supported by all HTML document elements.
     */
    public enum HtmlDocumentElementEvent {
        abort,
        auxclick,
        beforeinput,
        beforematch,
        beforetoggle,
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
     * Events supported by all HTML body elements, in addition to the events supported by all HTML document elements.
     */
    public enum HtmlBodyElementEvent {
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
     * All supported HTML event names are defined in the enums {@link HtmlDocumentElementEvent} and {@link HtmlBodyElementEvent}.
     * Any HTML event name which you wish to add to these enums can be supplied via this context-param.
     * Duplicates will be automatically filtered, case sensitive.
     */
    public static final String ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME = "jakarta.faces.ADDITIONAL_HTML_EVENT_NAMES";

    private enum CacheKey {
        HTML_DOCUMENT_ELEMENT_EVENT_NAMES,
        HTML_BODY_ELEMENT_EVENT_NAMES,
        FACES_ACTION_SOURCE_EVENT_NAMES,
        FACES_EDITABLE_VALUE_HOLDER_EVENT_NAMES;
    }

    private static final Map<CacheKey, Collection<String>> CACHE = new EnumMap<>(CacheKey.class);

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
        return cache(CacheKey.HTML_DOCUMENT_ELEMENT_EVENT_NAMES, () -> merge(getAdditionalHtmlEventNames(context), HtmlDocumentElementEvent.values()));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML body elements, including HTML document element event names.
     */
    public static Collection<String> getHtmlBodyElementEventNames(FacesContext context) {
        return cache(CacheKey.HTML_BODY_ELEMENT_EVENT_NAMES, () -> merge(getHtmlDocumentElementEventNames(context), HtmlBodyElementEvent.values()));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML implementations of Faces {@link ActionSource} components, including HTML body element event names.
     */
    public static Collection<String> getFacesActionSourceEventNames(FacesContext context) {
        return cache(CacheKey.FACES_ACTION_SOURCE_EVENT_NAMES, () -> merge(getHtmlBodyElementEventNames(context), FacesComponentEvent.action));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML implementations of Faces {@link EditableValueHolder} components, including HTML body element event names.
     */
    public static Collection<String> getFacesEditableValueHolderEventNames(FacesContext context) {
        return cache(CacheKey.FACES_EDITABLE_VALUE_HOLDER_EVENT_NAMES, () -> merge(getHtmlBodyElementEventNames(context), FacesComponentEvent.valueChange));
    }

    private static Collection<String> collect(Stream<String> stream) {
        return stream.sorted().distinct().collect(toUnmodifiableList());
    }

    private static Collection<String> merge(Collection<String> eventNames, Enum<?>... enumValues) {
        return collect(Stream.concat(Arrays.stream(enumValues).map(e -> e.name()), eventNames.stream()));
    }

    private static Collection<String> cache(CacheKey key, Supplier<Collection<String>> supplier) {
        return CACHE.computeIfAbsent(key, $ -> supplier.get());
    }
}
