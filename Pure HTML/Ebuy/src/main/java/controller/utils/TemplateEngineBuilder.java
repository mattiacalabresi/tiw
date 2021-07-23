package controller.utils;

import javax.servlet.ServletContext;

import org.eclipse.jdt.annotation.NonNull;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 * Factory class that creates a {@link TemplateEngine}.
 */
public class TemplateEngineBuilder {
	
	private TemplateEngineBuilder() {
		
	}
	
	@NonNull
	public static TemplateEngine build(@NonNull final ServletContext context) {
		final ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding(Values.Config.ENCODING_UTF_8);
		templateResolver.setSuffix(Values.Config.HTML_SUFFIX);
		
		final TemplateEngine templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		
		return templateEngine;
	}
}
