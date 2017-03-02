package neublick.locatemylot.model;

import java.io.Serializable;

// du la promotion thumb hay promotion detail thi deu dung class nay
public class Promotion implements Serializable {

	public static int DEFAULT_ID = -1;

	public int id = DEFAULT_ID;
	public String name;
	public String userName;
	public String promotionThumb;

	public long timeGet=-1;
	public long timeRedeem=-1;

	public String promotionImage;
	public String promotionContent;

	public Integer isPromotion;

	/*
	promotionThumb {id, promotionThumb}
	promotionDetail {id, promotionImage, promotionContent, isPromotion}
	 */

	// merge PromotionThumb voi PromotionDetail
	public void mergeWithPromotionThumb(Promotion other) {
		this.promotionThumb = other.promotionThumb;
	}

	@Override public String toString() {
		return String.format(
			"[id=%d,code=%s,userName=%s,promotionThumb=%s,timeGet=%d,timeRedeem=%]",
				(id >-1)? id: "null",
				(name != null)? name: "null",
				(promotionThumb != null)? promotionThumb: "null",
				(timeGet >-1)? timeGet: "null",
				(timeRedeem >-1)? timeRedeem: "null"
		);
	}

	public static class Factory {
		// neu promotion co id = 0 thi promotion khong lay dc tu internet
		public static Promotion fromThumb(String dataString) {
			String[] ss = dataString.split("~");
			Promotion result = new Promotion();
			try {
				result.id = Integer.parseInt(ss[0]);
				result.promotionThumb = ss[1];
			} catch (Exception ignored) {

			}
			return result;
		}

		public static Promotion fromDetail(String dataString) {
			String ss[] = dataString.split("~");
			Promotion result = new Promotion();
			try {
				result.id = Integer.parseInt(ss[0]);
				result.promotionImage = ss[1];
				result.promotionContent = ss[2];
				result.isPromotion = Integer.parseInt(ss[3]);
				/*
				String fmt = String.format(
					"id=%d, image=%s, is_promotion=%d",
					result.id,
					result.promotionImage,
					result.promotionContent,
					result.isPromotion
				);
				*/
				//le(dataString);
			} catch(Exception ignored) {
				le("Promotion/fromDetail error: "+ignored.getMessage());
			}
			return result;
		}
	}

	static void le(String fmt) {
		final String TAG = Promotion.class.getSimpleName();
		android.util.Log.e(TAG, fmt);
	}

	private static final long serialVersionUID = 0L;
}