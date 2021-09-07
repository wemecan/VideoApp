package com.kai.video.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class PickerHelper {
	public static PickerHelper getInstance() {
		return new PickerHelper();
	}
	public PickerHelper() {
		
	}
	public String get(List<Element> elements, OnGrepDocument grepDocument) {
		Element document = Jsoup.parse("<ul action='actionl'></ul>").getElementsByTag("ul").first();
		for (Element element : elements) {
			Element li = Jsoup.parse("<li></li>").getElementsByTag("li").first();
			String href = grepDocument.onGetHref(element);
			if (href == null || href.isEmpty()) {
				continue;
			}
			String title = grepDocument.onGetTitle(element);
			String subtitle = grepDocument.onGetSubtitle(element);
			li.attr("title",  subtitle.contains(title)?subtitle:title + "\t" +subtitle);
			li.attr("background", grepDocument.onGetBackground(element));
			li.attr("href", grepDocument.onGetHref(element));
			li.appendTo(document);
		}
		return document.outerHtml();
	}
	public interface OnGrepDocument{
		String onGetHref(Element e);
		String onGetTitle(Element e);
		String onGetSubtitle(Element e);
		String onGetBackground(Element e);
	}
}

