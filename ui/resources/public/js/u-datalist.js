// ../utils.ts
(function () {
var IS_BROWSER = typeof window !== "undefined" && typeof window.document !== "undefined" && typeof window.navigator !== "undefined";
var IS_ANDROID = IS_BROWSER && /android/i.test(navigator.userAgent);
var IS_IOS = IS_BROWSER && /iPad|iPhone|iPod/.test(navigator.userAgent);
var IS_SAFARI = IS_BROWSER && /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
IS_BROWSER && // @ts-expect-error Typescript has not implemented userAgentData yet https://stackoverflow.com/a/71392474
/^Mac/i.test(navigator.userAgentData?.platform || navigator.platform);
var SAFE_LABELLEDBY = `${IS_ANDROID ? "data" : "aria"}-labelledby`;
var SAFE_MULTISELECTABLE = `${IS_SAFARI ? "aria" : "data"}-multiselectable`;
var DISPLAY_BLOCK = ":host(:not([hidden])) { display: block }";
var FOCUS_OUTLINE = "outline: 1px dotted; outline: 5px auto Highlight; outline: 5px auto -webkit-focus-ring-color";
var UHTMLElement = typeof HTMLElement === "undefined" ? class {
} : HTMLElement;
function attr(el, name, value) {
  if (value === void 0) return el?.getAttribute(name) ?? null;
  if (value === null) el?.removeAttribute(name);
  else if (el?.getAttribute(name) !== value) el?.setAttribute(name, value);
  return null;
}
var events = (action, element, rest) => {
  for (const type of rest[0].split(",")) {
    rest[0] = type;
    Element.prototype[`${action}EventListener`].apply(element, rest);
  }
};
var on = (element, ...rest) => events("add", element, rest);
var off = (element, ...rest) => events("remove", element, rest);
var attachStyle = (element, css) => element.attachShadow({ mode: "open" }).append(
  createElement("slot"),
  // Unnamed slot does automatically render all top element nodes
  createElement("style", css)
);
var observers = /* @__PURE__ */ new WeakMap();
var mutationObserver = (element, options) => {
  if (options === void 0) return observers.get(element);
  try {
    observers.get(element).disconnect();
    observers.delete(element);
  } catch (err) {
  }
  if (options) {
    const observer = new MutationObserver(
      (detail) => element.handleEvent({ type: "mutation", detail })
    );
    observer.observe(element, options);
    observers.set(element, observer);
  }
};
var getRoot = (node) => {
  const root = node.getRootNode?.() || node.ownerDocument;
  return root instanceof Document || root instanceof ShadowRoot ? root : document;
};
var id = 0;
var useId = (el) => {
  if (!el) return "";
  if (!el.id) el.id = `:${el.nodeName.toLowerCase()}${(++id).toString(32)}`;
  return el.id;
};
var createElement = (tagName, text, attrs) => {
  const el = document.createElement(tagName);
  if (text) el.textContent = text;
  return el;
};
var customElements = {
  define: (name, instance) => !IS_BROWSER || window.customElements.get(name) || window.customElements.define(name, instance)
};

// u-option.ts
var DISABLED = "disabled";
var SELECTED = "selected";
var UHTMLOptionElement = class extends UHTMLElement {
  // Using ES2015 syntax for backwards compatibility
  static get observedAttributes() {
    return [DISABLED, SELECTED];
  }
  constructor() {
    super();
    attachStyle(
      this,
      `${DISPLAY_BLOCK}:host(:focus){${FOCUS_OUTLINE}}:host{ cursor: pointer }`
    );
  }
  connectedCallback() {
    if (!IS_IOS) this.tabIndex = -1;
    attr(this, "role", "option");
    this.attributeChangedCallback();
  }
  attributeChangedCallback() {
    attr(this, "aria-disabled", `${this.disabled}`);
    attr(this, "aria-selected", `${this.selected}`);
  }
  /** Sets or retrieves whether the option in the list box is the default item. */
  get defaultSelected() {
    return this[SELECTED];
  }
  set defaultSelected(value) {
    this[SELECTED] = value;
  }
  get disabled() {
    return attr(this, DISABLED) !== null;
  }
  set disabled(value) {
    attr(this, DISABLED, value ? "" : null);
  }
  /** Retrieves a reference to the form that the object is embedded in. */
  get form() {
    return this.closest("form");
  }
  /** Sets or retrieves the ordinal position of an option in a list box. */
  get index() {
    const options = this.closest("u-datalist")?.getElementsByTagName("u-option");
    return Array.from(options || [this]).indexOf(this);
  }
  /** Sets or retrieves a value that you can use to implement your own label functionality for the object. */
  get label() {
    return attr(this, "label") || this.text;
  }
  set label(value) {
    attr(this, "label", value);
  }
  get selected() {
    return attr(this, SELECTED) !== null;
  }
  set selected(value) {
    attr(this, SELECTED, value ? "" : null);
  }
  /** Sets or retrieves the text string specified by the option tag. */
  get text() {
    return this.textContent?.trim() || "";
  }
  set text(text) {
    this.textContent = text;
  }
  /** Sets or retrieves the value which is returned to the server when the form control is submitted. */
  get value() {
    return attr(this, "value") || this.text;
  }
  set value(value) {
    attr(this, "value", value);
  }
};
customElements.define("u-option", UHTMLOptionElement);

// u-datalist.ts
var IS_PRESS = false;
var IS_SAFARI_MAC = IS_SAFARI && !IS_IOS;
var EVENTS = "click,focusout,input,keydown,mousedown,mouseup";
var UHTMLDataListElement = class extends UHTMLElement {
  // Store sanitized value to speed up option filtering
  constructor() {
    super();
    // Using underscore instead of private fields for backwards compatibility
    // _announceCount = 0;
    // _announceTimer: ReturnType<typeof setTimeout> | number = 0;
    this._blurTimer = 0;
    this._input = null;
    this._root = null;
    this._value = "";
    attachStyle(
      this,
      `${DISPLAY_BLOCK}::slotted(u-option[disabled]) { display: none !important }`
      // Hide disabled options
    );
  }
  connectedCallback() {
    this.hidden = true;
    this._root = getRoot(this);
    attr(this, "role", "listbox");
    on(this._root, "focusin", this);
    on(this._root, "focus", this, true);
    setTimeout(() => {
      const inputs = this._root?.querySelectorAll(`input[list="${this.id}"]`);
      for (const input of inputs || [])
        attr(input, "aria-expanded", `${IS_SAFARI_MAC}`);
    });
  }
  disconnectedCallback() {
    off(this._root || this, "focus", this, true);
    off(this._root || this, "focusin", this);
    disconnectInput(this);
    this._root = null;
  }
  handleEvent(event) {
    const { type } = event;
    if (event.defaultPrevented) return;
    if (type === "click") onClick(this, event);
    if (type === "focus" || type === "focusin") onFocusIn(this, event);
    if (type === "focusout") onFocusOut(this);
    if (type === "keydown") onKeyDown(this, event);
    if (type === "mutation" || type === "input") setupOptions(this, event);
    if (type === "mouseup") IS_PRESS = false;
    if (type === "mousedown") IS_PRESS = this.contains(event.target);
  }
  get options() {
    return this.getElementsByTagName("u-option");
  }
};
var onFocusIn = (self, { target }) => {
  const isInput = self._input === target;
  const isInside = isInput || self.contains(target);
  if (isInside) return clearTimeout(self._blurTimer);
  if (!isInput && target instanceof HTMLInputElement && attr(target, "list") === self.id) {
    if (self._input) disconnectInput(self);
    self._input = target;
    self._input.autocomplete = "off";
    attr(self, SAFE_LABELLEDBY, useId(self._input.labels?.[0]));
    attr(self._input, "aria-autocomplete", "list");
    attr(self._input, "aria-controls", useId(self));
    attr(self._input, "role", "combobox");
    on(self._root || self, EVENTS, self);
    mutationObserver(self, {
      attributeFilter: ["value"],
      // Listen for value changes to show u-options
      attributes: true,
      childList: true,
      subtree: true
    });
    setExpanded(self, true);
  }
};
var onFocusOut = (self) => {
  if (!IS_PRESS) self._blurTimer = setTimeout(() => disconnectInput(self));
};
var onClick = (self, { target }) => {
  const isSingle = attr(self, SAFE_MULTISELECTABLE) !== "true";
  const option = [...self.options].find((opt) => opt.contains(target));
  if (self._input === target) {
    setExpanded(self, true);
  } else if (option) {
    for (const opt of self.options) {
      if (opt === option) opt.selected = true;
      else if (isSingle) opt.selected = false;
    }
    Object.getOwnPropertyDescriptor(
      HTMLInputElement.prototype,
      "value"
    )?.set?.call(self._input, option.value);
    if (isSingle) {
      self._input?.focus();
      setExpanded(self, false);
    }
    self._input?.dispatchEvent(
      new Event("input", { bubbles: true, composed: true })
    );
    self._input?.dispatchEvent(new Event("change", { bubbles: true }));
  }
};
var onKeyDown = (self, event) => {
  if (event.altKey || event.ctrlKey || event.metaKey || event.shiftKey) return;
  if (event.key !== "Escape") setExpanded(self, true);
  const { key } = event;
  const active = self._root?.activeElement;
  const options = getVisibleOptions(self);
  const index = options.indexOf(active);
  let next = -1;
  if (key === "ArrowDown") next = (index + 1) % options.length;
  if (key === "ArrowUp") next = (~index ? index : options.length) - 1;
  if (~index) {
    if (key === "Home" || key === "PageUp") next = 0;
    if (key === "End" || key === "PageDown") next = options.length - 1;
    if (key === "Enter") {
      options[index].click();
      return event.preventDefault();
    }
  }
  if (options[next]) for (const option of options) option.tabIndex = -1;
  if (options[next]) event.preventDefault();
  (options[next] || self._input)?.focus();
  if (key === "Escape") setExpanded(self, false);
};
var setExpanded = (self, open) => {
  self.hidden = !open;
  if (self._input)
    attr(self._input, "aria-expanded", `${IS_SAFARI_MAC || open}`);
  if (open) setupOptions(self);
};
var disconnectInput = (self) => {
  off(self._root || self, EVENTS, self);
  mutationObserver(self, false);
  setExpanded(self, false);
  self._input = null;
};
var getVisibleOptions = (self) => {
  return [...self.options].filter(
    (opt) => !opt.disabled && opt.offsetWidth && opt.offsetHeight
    // Checks disabled or visibility (since hidden attribute can be overwritten by display: block)
  );
};
var setupOptions = (self, event) => {
  const value = self._input?.value.toLowerCase().trim() || "";
  const hasChange = event?.type === "mutation" || self._value !== value;
  if (!hasChange) return;
  const hidden = self.hidden;
  const isSingle = attr(self, SAFE_MULTISELECTABLE) !== "true";
  const isTyping = event instanceof InputEvent && event.inputType;
  self.hidden = true;
  self._value = value;
  for (const opt of self.options) {
    const content = `${opt.value}${opt.label}${opt.text}`.toLowerCase();
    opt.hidden = !content.includes(value);
    if (isSingle && isTyping) opt.selected = false;
  }
  self.hidden = hidden;
  const visible = getVisibleOptions(self);
  if (IS_IOS)
    visible.map((opt, i, { length }) => {
      opt.title = `${i + 1}/${length}`;
    });
};
if (IS_BROWSER)
  Object.defineProperty(HTMLInputElement.prototype, "list", {
    configurable: true,
    enumerable: true,
    get() {
      return getRoot(this).getElementById(attr(this, "list") || "");
    }
  });
customElements.define("u-datalist", UHTMLDataListElement);

}());
