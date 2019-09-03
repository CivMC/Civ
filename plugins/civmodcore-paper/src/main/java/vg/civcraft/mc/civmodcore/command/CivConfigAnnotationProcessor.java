package vg.civcraft.mc.civmodcore.command;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedSourceVersion(value=SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("vg.civcraft.mc.civmodcore.command.CivCommand")
public class CivConfigAnnotationProcessor extends AbstractProcessor {
	
	public static final String fileLocation = "META-INF/civmodcore/civconfig";

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}
		Set<String> pluginClasses = new HashSet<>();
		Elements elements = processingEnv.getElementUtils();
		for (Element element : roundEnv.getElementsAnnotatedWith(CivCommand.class)) {
			CivCommand annot = element.getAnnotation(CivCommand.class);
			if (annot == null) {
				// ????
				continue;
			}
			if (!validConstructor(element)) {
				return true;
			}
			pluginClasses.add(elements.getBinaryName((TypeElement) element).toString());
		}
		// load existing
		Filer filer = processingEnv.getFiler();
		try {
			FileObject file = filer.getResource(StandardLocation.CLASS_OUTPUT, "", fileLocation);
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.openInputStream(), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				pluginClasses.add(line);
			}
			reader.close();
		} catch (java.nio.file.NoSuchFileException | FileNotFoundException x) {
			// doesn't exist yet, that's fine
		} catch (IOException x) {
			processingEnv.getMessager().printMessage(Kind.ERROR,
					"Failed to load existing service definition files: " + x);
		}

		// write back
		try {
			FileObject f = filer.createResource(StandardLocation.CLASS_OUTPUT, "", fileLocation);
			PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), "UTF-8"));
			processingEnv.getMessager().printMessage(Kind.NOTE, "Writing " + f.getName());
			for (String value : pluginClasses) {
				printWriter.println(value);
			}
			printWriter.close();
		} catch (IOException x) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write service definition files: " + x);
		}
		return true;
	}

	private boolean validConstructor(Element el) {
		for (Element subelement : el.getEnclosedElements()) {
			if (subelement.getKind() == ElementKind.CONSTRUCTOR) {
				if (!subelement.getModifiers().contains(Modifier.PUBLIC)) {
					processingEnv.getMessager().printMessage(Kind.ERROR,
							"Invalid constructor visibility for plugin " + subelement.toString());
					return false;
				}
				ExecutableType mirror = (ExecutableType) subelement.asType();
				if (!mirror.getParameterTypes().isEmpty()) {
					processingEnv.getMessager().printMessage(Kind.ERROR,
							"Invalid constructor for plugin, taking arguments is not allowed: " + subelement.toString());
					return false;
				}
			}
		}
		return true;
	}
}

