package v6ak.tools.muni.timetableToICal

import resource.managed
import xml.XML
import Console.err
import net.fortuna.ical4j.model.component.VEvent
import org.scala_tools.time.Imports._
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.{Date=>JDate}
import net.fortuna.ical4j.model.property._
import net.fortuna.ical4j.model.{Property=>IProperty, Dur, Recur, Date=>IDate, Calendar=>ICalendar, DateTime => IDateTime}

class MyDtStart(dateTime:IDateTime) extends IProperty("DTSTART"){ // Chro chro. Ale jinak mi to fakt nešlo.
	var value = dateTime.toString+"Z"
	def getValue = value
	def setValue(x:String){value = x}
	def validate(){}
}

/**
 * @author Vít Šesták 'v6ak'
 */
object App {

	final val Days = Map(
		"Po"-> 1,
		"Mon"->1,
		"Út"-> 2,
		"Tue"->2,
		"St"-> 3,
		"Wed"->3,
		"Čt"-> 4,
		"Thu"->4,
		"Pá"-> 5,
		"Fri"->5,
		"So"-> 6,
		"Sat"->6,
		"Ne"-> 7,
		"Sun"->7
	)

	def main(args : Array[String]) {
		if(args.length != 3){
			err println "usage: <appLauncher> from dateTo"
			err println "dateTo: YYYY-MM-DD"
			System exit 1
		}

		val until = new IDate(new SimpleDateFormat("y-M-d").parse(args(2)).getTime+24*60*60*1000)
		val from = args(0)
		val to = args(1)
		val xml = XML loadFile from
		val calendar = new ICalendar

		val startDate = DateTime.now
		def date(day:Int, time: String) = {
			val timeParts = time split ':'
			if(timeParts.length != 2){
				throw new Exception("Invalid time: "+time)
			}
			val d = startDate.withDayOfWeek(day).withTime(timeParts(0).toInt, timeParts(1).toInt, 0, 0)
			d.getMillis
		}

		xml\"tabulka"\"den" foreach { den =>
			val dayName = den.attribute("id").get.toString
			val day = Days(dayName)
			den\"radek"\"slot" foreach { slot =>
				val fromTime = slot.attribute("odcas").get.toString
				val toTime = slot.attribute("docas").get.toString
				val place = (slot\"mistnost").text
				val akce = slot\"akce"
				val code = (akce\"kod").text
				val name = (akce\"nazev").text
				val fromMilis = date(day, fromTime)
				val toMilis = date(day, toTime)
				val event = new VEvent(new IDate(new JDate(fromMilis)), new Dur(new JDate(fromMilis), new JDate(toMilis)), code+"@"+place+": "+name)
				List(
					"DTSTAMP",
					"DTSTART"
				) foreach { name =>
					event.getProperties.removeAll(event.getProperties(name))
				}
				List(
					new DtStamp(new IDateTime(fromMilis)),
					new MyDtStart(new IDateTime(fromMilis)),
					new RRule(new Recur(Recur.WEEKLY, until))
				) foreach {
					event.getProperties.add(_)
				}
				calendar.getComponents.add(event)
			}
		}
		for(out <- managed(new FileWriter(to))){
			out write calendar.toString
		}
	}

}