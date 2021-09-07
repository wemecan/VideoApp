package com.kai.video;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.kai.video.tool.JsoupHelper;
import com.kai.video.tool.PickerHelper;

public class Commend {
	private String type, action;
	public Commend(String type, String action) {
		this.type = type;
		this.action = action;
	}
	public String get() {
		if (type.equals("iqiyi")) {
			switch (action) {
			case "tv":
				return getIqiyiTV();
			case "film":
				return getIqiyiFilm();
			case "cartoon":
				return getIqiyiCartoon();
			case "zy":
				return getIqiyiZY();
			default:
				return "";
			}
		}else if (type.equals("tencent")) {
			switch (action) {
			case "tv":
				return getTencentTV();
			case "film":
				return getTencentFilm();
			case "cartoon":
				return getTencentCartoon();
			case "zy":
				return getTencentZY();
			default:
				return "";
			}
		}else if (type.equals("bilibili")) {
			switch (action) {
			case "tv":
				return getBiliBiliTV();
			case "film":
				return getBiliBiliFilm();
			case "japaneseList":
				return getBiliBiliJapan();
			case "ChineseList":
				return getBiliBiliChina();
			case "zy":
				return getBiliBiliZY();
			default:
				return "";
			}
		}else if (type.equals("mgtv")) {
			switch (action) {
			case "tv":
				return getMgtvTV();
			case "film":
				return getMgtvFilm();
			case "zy":
				return getMgtvZY();
			default:
				return "";
			}
		}
		return "";
	}
	public Element getDocument(){
		Element document = Jsoup.parse(get()).getElementsByTag("ul").first();
		if (document == null)
			return null;
		if (action.equals("japaneseList"))
			type = "bilibili1";
		document = document.attr("actionk", type);
		return document;
	}
	private String getMgtvZY() {
		try {
			Document document = Jsoup.connect("https://m.mgtv.com/channel/show/")
					.userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:swiper-slide");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("title").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("dec").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.getElementsByTag("a").first().attr("abs:href").replaceAll("\\?.*", "");
						if (!href.contains("m.mgtv.com/b")) {
							href = null;
						}
						return href.replace("m.mgtv.com", "www.mgtv.com");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						String img = e.getElementsByClass("img").first().attr("style");
						Pattern pattern = Pattern.compile("background-image:url\\((.*)\\);");
						Matcher matcher = pattern.matcher(img);
						if (matcher.find()) {
							return matcher.group(1);
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private String getMgtvFilm() {
		try {
			Document document = Jsoup.connect("https://m.mgtv.com/channel/movie/")
					.userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:swiper-slide");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("title").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("dec").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.getElementsByTag("a").first().attr("abs:href").replaceAll("\\?.*", "");
						if (!href.contains("m.mgtv.com/b")) {
							href = null;
						}
						return href.replace("m.mgtv.com", "www.mgtv.com");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						String img = e.getElementsByClass("img").first().attr("style");
						Pattern pattern = Pattern.compile("background-image:url\\((.*)\\);");
						Matcher matcher = pattern.matcher(img);
						if (matcher.find()) {
							return matcher.group(1);
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	private String getMgtvTV() {
		try {
			Document document = Jsoup.connect("https://m.mgtv.com/channel/tv/")
					.userAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:swiper-slide");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("title").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("dec").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.getElementsByTag("a").first().attr("abs:href").replaceAll("\\?.*", "");
						if (!href.contains("m.mgtv.com/b")) {
							href = null;
						}
						return href.replace("m.mgtv.com", "www.mgtv.com");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						String img = e.getElementsByClass("img").first().attr("style");
						Pattern pattern = Pattern.compile("background-image:url\\((.*)\\);");
						Matcher matcher = pattern.matcher(img);
						if (matcher.find()) {
							return matcher.group(1);
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	private String getBiliBiliFilm() {
		try {
			Document document = Jsoup.connect("https://www.bilibili.com/movie/")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:bg-item");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("title").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("desc").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.getElementsByTag("a").first().attr("abs:href").replaceAll("\\?.*", "");
						if (!href.contains("bangumi")) {
							href = null;
						}
						return href;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("img").first().attr("abs:src");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	private String getBiliBiliZY() {
		try {
			Document document = Jsoup.connect("https://www.bilibili.com/documentary/")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:carou-images-wrapper/class:chief-recom-item");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("img").first().attr("alt");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.getElementsByTag("a").first().attr("abs:href").replaceAll("\\?.*", "");
						if (!href.contains("bangumi")) {
							href = null;
						}
						return href;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("img").first().attr("abs:src");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	private String getBiliBiliChina() {
		try {
			Document document = Jsoup.connect("https://www.bilibili.com/guochuang/")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:carou-images-wrapper/class:chief-recom-item");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("img").first().attr("alt");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.getElementsByTag("a").first().attr("abs:href").replaceAll("\\?.*", "");
						if (!href.contains("bangumi")) {
							href = null;
						}
						return href;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("img").first().attr("abs:src");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private String getBiliBiliJapan() {
		try {
			Document document = Jsoup.connect("https://www.bilibili.com/anime/")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:carou-images-wrapper/class:chief-recom-item");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("img").first().attr("alt");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.getElementsByTag("a").first().attr("abs:href").replaceAll("\\?.*", "");
						if (!href.contains("bangumi")) {
							href = null;
						}
						return href;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("img").first().attr("abs:src");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private String getBiliBiliTV() {
		try {
			Document document = Jsoup.connect("https://www.bilibili.com/tv/")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:carou-images-wrapper/class:chief-recom-item");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("img").first().attr("alt");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.getElementsByTag("a").first().attr("abs:href").replaceAll("\\?.*", "");
						if (!href.contains("bangumi")) {
							href = null;
						}
						return href;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("img").first().attr("abs:src");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private String getTencentZY() {
		try {
			Document document = Jsoup.connect("https://v.qq.com/channel/variety")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "id:new_vs_focus/class:nav_link");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("title_text").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("span").first().attr("title");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.attr("abs:href");
						if (!href.startsWith("http://v.qq.com/x/cover/")) {
							href = null;
						}
						return href;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.attr("abs:data-bgimage");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private String getTencentCartoon() {
		try {
			Document document = Jsoup.connect("https://v.qq.com/channel/cartoon")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "id:new_vs_focus/class:nav_link");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("title_text").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("span").first().attr("title");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.attr("abs:href");
						if (!href.startsWith("https://v.qq.com/x/cover/")) {
							href = null;
						}
						return href;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.attr("abs:data-bgimage");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	private String getTencentFilm() {
		try {
			Document document = Jsoup.connect("https://v.qq.com/channel/movie")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "id:new_vs_focus/class:nav_link");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("title_text").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("span").first().attr("title");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.attr("abs:href");
						if (!href.startsWith("https://v.qq.com/x/cover/")) {
							href = null;
						}
						return href;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.attr("abs:data-bgimage");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	private String getTencentTV() {
		try {
			Document document = Jsoup.connect("https://v.qq.com/channel/tv")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "id:new_vs_focus/class:nav_link");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByClass("title_text").first().text();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("span").first().attr("title");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						String href = e.attr("abs:href");
						if (!href.startsWith("https://v.qq.com/x/cover/")) {
							href = null;
						}
						return href;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.attr("abs:data-bgimage");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return "";
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	private String getIqiyiCartoon() {
		try {
			Document document = Jsoup.connect("https://www.iqiyi.com/cooperate/hao123/dongman.html")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:focus-img-wrap/class:img-list/tag:li");
			String aString =  PickerHelper.getInstance().get(elements.subList(0, 5), new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("a").attr("title");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return "";
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("a").attr("abs:href");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						String background = e.attr("abs:data-webp-img");
						return background;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	private String getIqiyiFilm() {
		try {
			Document document = Jsoup.connect("https://www.iqiyi.com/cooperate/pcw/dianying.html?vfm=m_771_hao&fv=534cea53c5350b116c9065c7fada7707")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:focus-img-wrap/class:img-list/tag:li");
			String aString =  PickerHelper.getInstance().get(elements.subList(0, 5), new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("a").attr("data-indexfocus-currenttitleelem");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("a").attr("data-indexfocus-description");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("a").attr("abs:href");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						String background = e.getElementsByTag("a").attr("abs:data-webp-img");
						return background.isEmpty()?e.attr("abs:data-indexfocus-lazyimg"):background;
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private String getIqiyiZY() {
		try {
			Document document = Jsoup.connect("https://www.iqiyi.com/cooperate/pcw/zongyi.html")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:focus-img-wrap/class:img-list/tag:li");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					return "";
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return "";
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("a").attr("abs:href");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						String backString = e.attr("style");
						Pattern pattern = Pattern.compile("url\\((.*)\\);");
						Matcher matcher = pattern.matcher(backString);
						if (matcher.find()) {
							return "https:" + matcher.group(1);
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private String getIqiyiTV() {
		try {
			Document document = Jsoup.connect("https://www.iqiyi.com/cooperate/pcw/dianshiju.html?vfm=m_771_hao&fv=534cea53c5350b116c9065c7fada7707")
					.get();
			List<Element> elements = JsoupHelper.searchGroup(document, "class:focus-img-wrap/class:img-list/tag:li");
			String aString =  PickerHelper.getInstance().get(elements, new PickerHelper.OnGrepDocument() {
				
				@Override
				public String onGetTitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("a").attr("data-indexfocus-currenttitleelem");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetSubtitle(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("a").attr("data-indexfocus-description");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetHref(Element e) {
					// TODO Auto-generated method stub
					try {
						return e.getElementsByTag("a").attr("abs:href");
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
				
				@Override
				public String onGetBackground(Element e) {
					// TODO Auto-generated method stub
					try {
						String backString = e.attr("style");
						Pattern pattern = Pattern.compile("url\\((.*)\\);");
						Matcher matcher = pattern.matcher(backString);
						if (matcher.find()) {
							return "https:" + matcher.group(1);
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}
					return null;
				}
			});
			return aString;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
		
		
	}
}
