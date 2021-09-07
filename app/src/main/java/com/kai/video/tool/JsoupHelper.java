package com.kai.video.tool;


import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class JsoupHelper {
	public static Element search(Element father,String path) {
		if (path.isEmpty()) {
			return father;
		}
		try {
			String[] paths = path.split("/");
			Element next=father;
			for(String p:paths) {
				//拆分类似与元素
				next = getElement(p, next);
				
			}
			return next;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		
	}
	public static List<Element> filter(List<Element> former,String path) {
		List<Element> later=new ArrayList<Element>();
		for (Element f : former) {
			later.add(search(f, path));
		}
		return later;
	}


	
	public static Element getElement(String p,Element next) throws Exception {
		String[] attributes = p.split(":");
		String type = attributes[0];
		String param = attributes[1];
		switch (type) {
		case "class":
			next = next.getElementsByClass(param).first();
			break;
		case "tag":
			next = next.getElementsByTag(param).first();
			break;
		case "id":
			next = next.getElementById(param);
			break;
		case "name":
			next = next.getElementsByAttributeValue("name", param).first();
			break;
		case "action":
			if (param.equals( "next")) {
				next = next.nextElementSibling();
			} else if (param.equals("pre")) {
				next = next.previousElementSibling();
			} else if (param.equals("parent")) {
				next = next.parent();
			}
			break;
		default:
			break;
		}
		return next;
	}
	
	public static List<Element> getElements(String p,Element next) throws Exception {
		List<Element> elements=new ArrayList<Element>();
		String[] attributes = p.split(":");
		String type = attributes[0];
		String param = attributes[1];
		switch (type) {
		case "class":
			elements = next.getElementsByClass(param);
			break;
		case "tag":
			elements = next.getElementsByTag(param);
			break;
		case "id":
			break;
		default:
			break;
		}
		return elements;
	}
	
	public static List<Element> searchGroup(Element father,String path) {
		try {
			String[] paths = path.split("/");
			Element next=father;
			for (int i = 0; i < paths.length; i++) {
				String p=paths[i];
				if (i==paths.length-1) {
					return getElements(p, next);
				}
				next = getElement(p, next);
			}
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		return null;
		
	}
}
