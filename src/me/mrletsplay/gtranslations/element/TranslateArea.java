package me.mrletsplay.gtranslations.element;

import java.util.function.Supplier;

import me.mrletsplay.webinterfaceapi.html.HtmlElement;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.WebinterfaceAction;
import me.mrletsplay.webinterfaceapi.webinterface.page.element.AbstractWebinterfacePageElement;

public class TranslateArea extends AbstractWebinterfacePageElement {
	
	private Supplier<String>
		placeholder,
		initialValue;
	
	private WebinterfaceAction onChangeAction;
	
	public TranslateArea(Supplier<String> placeholder, Supplier<String> initialValue) {
		this.placeholder = placeholder;
		this.initialValue = initialValue;
	}
	
	public TranslateArea(String placeholder, String initialValue) {
		this(() -> placeholder, () -> initialValue);
	}
	
	public TranslateArea(Supplier<String> placeholder) {
		this(placeholder, null);
	}
	
	public TranslateArea(String placeholder) {
		this(() -> placeholder);
	}
	
	public TranslateArea() {
		this(() -> "Text");
	}
	
	public void setPlaceholder(Supplier<String> placeholder) {
		this.placeholder = placeholder;
	}
	
	public void setPlaceholder(String placeholder) {
		setPlaceholder(() -> placeholder);
	}
	
	public Supplier<String> getPlaceholder() {
		return placeholder;
	}
	
	public void setInitialValue(Supplier<String> initialValue) {
		this.initialValue = initialValue;
	}
	
	public void setInitialValue(String initialValue) {
		setInitialValue(() -> initialValue);
	}
	
	public Supplier<String> getInitialValue() {
		return initialValue;
	}
	
	public void setOnChangeAction(WebinterfaceAction onChangeAction) {
		this.onChangeAction = onChangeAction;
	}
	
	@Override
	public HtmlElement createElement() {
		HtmlElement b = new HtmlElement("textarea");
		b.setAttribute("placeholder", placeholder);
		b.setAttribute("aria-label", placeholder);
		b.setAttribute("maxlength", "512");
		b.appendAttribute("style", "resize:vertical;background-color:var(--theme-color-content-bg);border:1px solid var(--theme-color-content-border);");
		if(initialValue != null) {
			String v = initialValue.get();
			if(v != null) b.setAttribute("value", v);
		}
		if(onChangeAction != null) b.setAttribute("onchange", onChangeAction.createAttributeValue());
		return b;
	}

}
