# TAQ NYSE OpenBook Ultra Parser

Scala parser for NYSE historical data product: http://www.nyxdata.com/Data-Products/NYSE-OpenBook-History

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