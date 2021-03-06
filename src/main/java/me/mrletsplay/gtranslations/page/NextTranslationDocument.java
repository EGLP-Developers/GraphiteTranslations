package me.mrletsplay.gtranslations.page;

import me.mrletsplay.gtranslations.GraphiteTranslations;
import me.mrletsplay.gtranslations.TranslationData;
import me.mrletsplay.gtranslations.TranslationStatus;
import me.mrletsplay.simplehttpserver.http.HttpStatusCodes;
import me.mrletsplay.simplehttpserver.http.document.HttpDocument;
import me.mrletsplay.simplehttpserver.http.request.HttpRequestContext;

public class NextTranslationDocument implements HttpDocument {
	
	@Override
	public void createContent() {
		HttpRequestContext ctx = HttpRequestContext.getCurrentContext();
		
		String locale = ctx.getClientHeader().getPath().getQuery().getFirst("locale");
		String mode = ctx.getClientHeader().getPath().getQuery().getFirst("mode");
		if(locale == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.FOUND_302);
			ctx.getServerHeader().getFields().set("Location", "/");
			return;
		}
		
		TranslationData next = GraphiteTranslations.getTranslationData().stream()
				.filter(t -> t.getLocaleIdentifier().equals(locale) && (mode != null && mode.equals("t") ? t.getStatus() == TranslationStatus.SUBMITTED : t.getStatus().equals(TranslationStatus.NOT_TRANSLATED)))
				.sorted(TranslationsPage.COMPARATOR)
				.findFirst().orElse(null);
		
		if(next == null) {
			ctx.getServerHeader().setStatusCode(HttpStatusCodes.FOUND_302);
			ctx.getServerHeader().getFields().set("Location", "/t/translations?locale=" + locale);
			return;
		}
		
		ctx.getServerHeader().setStatusCode(HttpStatusCodes.FOUND_302);
		ctx.getServerHeader().getFields().set("Location", "/t/translate?locale=" + locale + "&path=" + next.getPath());
	}

}
