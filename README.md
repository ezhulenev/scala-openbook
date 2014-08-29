# TAQ NYSE OpenBook Ultra Parser

Scala parser for NYSE historical data product: http://www.nyxdata.com/Data-Products/NYSE-OpenBook-History

## Where to get it

To get the latest version of the library, add the following to your SBT build:

``` scala
resolvers += "Scalafi Bintray Repo" at "http://dl.bintray.com/ezhulenev/releases"
```

And use following library dependencies:

```
libraryDependencies +=  "com.scalafi" %% "scala-openbook" % "0.0.2"
```

## Order Book attributes/features extraction

Order book attributes/features extraction based on [Modeling high-frequency limit order book dynamics with support vector machines](https://raw.github.com/ezhulenev/scala-openbook/master/assets/Modeling-high-frequency-limit-order-book-dynamics-with-support-vector-machines.pdf) paper.

> We identify a collection of proposed attributes that are divided into three categories: basic, time-insensitive, and time-sensitive,
> all of which can be directly computed from the data. Attributes in the basic set are the prices and volumes at both ask and bid sides
> up to n = 10 different levels (that is, price levels in the order book at a given moment), which can be directly fetched from the
> order book. Attributes in the time-insensitive set are easily computed from the basic set at a single point in time.
> Of this, bid-ask spread and mid-price, price ranges, as well as average price and volume at different price levels are calculated
> in feature sets v2, v3, and v5, respectively; while v5 is designed to track the accumulated differences of price and volume
> between ask and bid sides. By further taking the recent history of current data into consideration,
> we devise the features in the time-sensitive set.


##### Order book messages

![Message book](https://raw.github.com/ezhulenev/scala-openbook/master/assets/messagebook.png)


##### Feature sets

![Order book feature sets](https://raw.github.com/ezhulenev/scala-openbook/master/assets/features.png)

## Example

``` scala
import com.scalafi.openbook.orderbook._

object Example extends App {

  import scalaz.std.option._
  import scalaz.syntax.applicative._

  implicit val codec = io.Codec.ISO8859

  val ticker = "APL"
  val source = this.getClass.getResource("/openbookultraAA_N20130403_1_of_1").getPath

  val orders = OpenBookMsg.stream(source).
    filter(_.symbol == ticker)

  val basicSet = BasicSet(ticker, orders)
  val timeInsensitiveSet = TimeInsensitiveSet(ticker, orders)

  val ask1 = basicSet.askPrice(1)
  val bid1 = basicSet.bidPrice(1)
  val spread1 = timeInsensitiveSet.priceSpread(1)

  val features = ((ask1 zip bid1) zipWith spread1)((ab, s) => (ab._1, ab._2, s))
  
  features.runLog.run.foreach {
    case (ask, bid, spread) =>
      println(s"Ask: $ask. Bid: $bid. Spread: $spread")
      ^^(ask, bid, spread)((a, b, s) => assume(a - b == s))
  }
}
```

####### Output

```
Ask: None. Bid: None. Spread: None
Ask: None. Bid: None. Spread: None
Ask: None. Bid: Some(178000). Spread: None
Ask: None. Bid: Some(290500). Spread: None
Ask: Some(420000). Bid: Some(290500). Spread: Some(129500)
Ask: Some(380000). Bid: Some(290500). Spread: Some(89500)
Ask: Some(380000). Bid: Some(322000). Spread: Some(58000)
```
