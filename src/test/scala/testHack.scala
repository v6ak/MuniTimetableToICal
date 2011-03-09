
import org.junit._
import v6ak.tools.muni.timetableToICal.App

class AppTest {

	private def run(start:String) = App main Array(start+".xml", start+".ics", "2011-04-27")

	@Test
	def testApp(){
		run(AppTestCfg.Path+"/2011.spring")
		println()
		run(AppTestCfg.Path+"/2011.spring.cs")
	}


}


