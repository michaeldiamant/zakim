package mld.zakim

import java.nio.ByteBuffer

import com.typesafe.scalalogging.StrictLogging

object ParserDemo extends StrictLogging {

  private case class MutableBidRequest(
    var regs: Option[Position],
    var w: Option[Position],
    var h: Option[Position],
    var page: Option[Position])
  private object MutableBidRequest {
    val empty = new MutableBidRequest(None, None, None, None)
  }

  private val exampleJson = ByteBuffer.wrap(
    """{"regs":{"coppa":0},"imp":[{"banner":{"w":728,"h":90,"pos":1,"topframe":0},"secure":0,"id":"1"}],"site":{"cat":["IAB1"],"page":"http://photos.toofab.com/galleries/this_weeks_best_celebrity_twitpics_59","ref":"http://photos.toofab.com/galleries/mtv_vjs__then__now?utm_source=zergnet.com&utm_medium=referral&utm_campaign=zergnet_209141&adid=zergnetinternal","content":{"keywords":"2,6268"},"id":"142276","publisher":{"id":"177754"}},"user":{"buyeruid":"enyhhpu5humhj","id":"Vg05S8AoJIwAAGwk3bYAAADT"},"id":"0B79190122C09178","ext":{"ssl":0,"sdepth":1,"edepth":19},"device":{"ua":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36","geo":{"country":"USA","city":"Clermont","zip":"34714","lat":28.5494,"region":"FL","lon":-81.7729,"type":2,"metro":"534"},"language":"EN","devicetype":2,"ip":"50.89.28.5","os":"OS X"}} """
      .getBytes("UTF-8"))
  private val simpleJson5 = ByteBuffer.wrap(
    """   {"regs":"0",
      "banner":{"w":728,"h":"90"},"deviceType":"android"}""".stripMargin.getBytes("UTF-8"))
  private val invalidArrayJson = ByteBuffer.wrap(
    """{"regs":{"coppa":0},"imp":[{"banner":{"w":728,"h":90,"pos":1,"topframe":0},"secure":0,"id":"1"},"banner":{"w":320,"h":240}]}"""
      .getBytes("UTF-8"))
  private val arrayJson = ByteBuffer.wrap(
    """{"regs":{"coppa":0},"imp":[{"banner":{"w":728,"h":90,"pos":1,"topframe":0},"secure":0,"id":"1"},{"banner":{"w":320,"h":240}}]}"""
      .getBytes("UTF-8"))

  def main(args: Array[String]): Unit = {

    val req = MutableBidRequest.empty
    val json = arrayJson
    val _ = Parser.parse(json, req)(
      (key, p, r) => {
        logger.debug("key = " + key)
        if (key == JsonPath("imp.banner.w")) r.w = Some(p)
        else if (key == JsonPath("imp.banner.h")) r.h = Some(p)
        else if (key == JsonPath("regs")) r.regs = Some(p)
        else if (key == JsonPath("site.page")) r.page = Some(p)
        r
      })

    logger.debug(req.toString)
    req.w.foreach { case Position(StartIndex(s), EndIndex(e)) =>
      json.position(s)
      val arr = new Array[Byte](e - s)
      val zz = Integer.parseInt(new String({
        json.get(arr, 0, e - s)
        arr
      }, "UTF-8"))
      logger.debug(s"ad size width = $zz")
    }

    req.page.foreach { case Position(StartIndex(s), EndIndex(e)) =>
      json.position(s)
      val arr = new Array[Byte](e - s)
      val zz = new String({
        json.get(arr, 0, e - s)
        arr
      }, "UTF-8")
      logger.debug(s"page = $zz")
    }
  }
}
