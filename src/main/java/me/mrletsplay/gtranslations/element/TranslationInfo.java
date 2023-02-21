package me.mrletsplay.gtranslations.element;

import java.util.Comparator;
import java.util.List;

import me.mrletsplay.gtranslations.TranslationData;
import me.mrletsplay.gtranslations.TranslationStatus;
import me.mrletsplay.simplehttpserver.dom.html.HtmlElement;
import me.mrletsplay.simplehttpserver.dom.html.element.HtmlButton;
import me.mrletsplay.webinterfaceapi.context.WebinterfaceContext;
import me.mrletsplay.webinterfaceapi.page.action.RedirectAction;
import me.mrletsplay.webinterfaceapi.page.element.AbstractPageElement;
import me.mrletsplay.webinterfaceapi.page.element.layout.DefaultLayoutOption;
import me.mrletsplay.webinterfaceapi.util.WebinterfaceUtils;

public class TranslationInfo extends AbstractPageElement {
	
	public static final Comparator<TranslationData> COMPARATOR;
	
	static {
		Comparator<TranslationData> d = Comparator.comparingInt(t -> t.checkErrors().isEmpty() || t.getStatus() == TranslationStatus.ACCEPTED ? 1 : 0);
		d = d.thenComparing(t -> t.getStatus());
		d = d.thenComparing(t -> t.getPath());
		COMPARATOR = d;
	}

	private List<TranslationData> data;
	
	public TranslationInfo(List<TranslationData> data) {
		this.data = data;
		addLayoutOptions(DefaultLayoutOption.NO_PADDING);
	}

	@Override
	public HtmlElement createElement() {
		data.sort(COMPARATOR);
		
		WebinterfaceContext.getCurrentContext().includeStyleSheet("gtranslations.css");
		
		HtmlElement p = new HtmlElement("div");
		p.addClass("translation-info");
		
		for(TranslationData d : data) {
			boolean issues = !d.checkErrors().isEmpty() && d.getStatus() != TranslationStatus.ACCEPTED;
			String statusMessage = issues ? "Possible Issues" : d.getStatus().getFriendlyName();
			
			HtmlElement status = new HtmlElement("div");
			status.addClass("translation-status");
			status.setAttribute("style", "background-color: " + (issues ? "orange" : d.getStatus().getColor()));
			status.setAttribute("title", statusMessage);
			p.appendChild(status);
			
			HtmlElement path = new HtmlElement("span");
			path.setText(d.getPath());
			p.appendChild(path);
			
			HtmlElement statusMsg = new HtmlElement("span");
			statusMsg.setText(statusMessage);
			p.appendChild(statusMsg);
			
			HtmlButton edit = HtmlElement.button();
			edit.appendChild(WebinterfaceUtils.iconifyIcon("mdi:edit"));
			edit.setOnClick(RedirectAction.to("/t/translate?locale=" + d.getLocaleIdentifier() + "&path=" + d.getPath()).createAttributeValue());
			p.appendChild(edit);
		}
		
		return p;
	}

}
