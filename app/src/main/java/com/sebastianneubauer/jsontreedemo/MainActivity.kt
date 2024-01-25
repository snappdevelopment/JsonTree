package com.sebastianneubauer.jsontreedemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.TreeColors
import com.sebastianneubauer.jsontree.JsonTree
import com.sebastianneubauer.jsontree.JsonTree2Ui
import com.sebastianneubauer.jsontree.TreeState
import com.sebastianneubauer.jsontree.defaultDarkColors
import com.sebastianneubauer.jsontree.defaultLightColors
import com.sebastianneubauer.jsontreedemo.ui.theme.JsonTreeTheme
import java.lang.IllegalStateException

internal class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JsonTreeTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    var errorMessage: String? by remember { mutableStateOf(null) }
                    var json: String by remember { mutableStateOf(simpleJson) }
                    var colors: TreeColors by remember { mutableStateOf(defaultLightColors) }
                    var initialState: TreeState by remember { mutableStateOf(TreeState.FIRST_ITEM_EXPANDED) }

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "🌳 JsonTree",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                            json = when (json) {
                                emptyJson -> simpleJson
                                simpleJson -> complexJson
                                complexJson -> emptyJson
                                else -> throw IllegalStateException("No JSON selected!")
                            }
                        }
                    ) {
                        Text(
                            text = when (json) {
                                simpleJson -> "Simple Json"
                                emptyJson -> "Empty Json"
                                complexJson -> "Complex Json"
                                else -> throw IllegalStateException("No JSON selected!")
                            }
                        )
                    }

                    Button(
                        onClick = {
                            colors = if(colors == defaultLightColors) defaultDarkColors else defaultLightColors
                        }
                    ) {
                        Text(text = if(colors == defaultLightColors) "Light" else "Dark")
                    }

                    Button(
                        onClick = {
                            val newState = when(initialState) {
                                TreeState.EXPANDED -> TreeState.COLLAPSED
                                TreeState.COLLAPSED -> TreeState.FIRST_ITEM_EXPANDED
                                TreeState.FIRST_ITEM_EXPANDED -> TreeState.EXPANDED
                            }
                            initialState = newState
                        }
                    ) {
                        Text(text = initialState.name)
                    }

                    val pagerState = rememberPagerState(0)

                    //Pager to test leaving composition
                    HorizontalPager(
                        pageCount = 3,
                        state = pagerState
                    ) { pageIndex ->
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            when (pageIndex) {
                                0 -> {
                                    JsonTree2Ui(
                                        modifier = Modifier.background(
                                            if(colors == defaultLightColors) Color.White else Color.Black
                                        ),
                                        json = json,
                                        initialState = initialState,
                                        colors = colors,
                                        onError = { errorMessage = it.localizedMessage },
                                    )
                                }
                                1 -> {
                                    Text(text = "Page 1")
                                }
                                2 -> {
                                    Text(text = "Page 2")
                                }
                            }
                        }
                    }

                    errorMessage?.let {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = it,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    private val complexJson = """
        {
        	"ctRoot": [
        		{
        			"_id": "P4PFCX0HLYH3BFI6",
        			"name": "Ahmed Doyle",
        			"dob": "2020-02-11",
        			"address": {
        				"street": "8749 Weston Road",
        				"town": "Chichester",
        				"postode": "SR88 4OA"
        			},
        			"telephone": "+503-1138-224-409",
        			"pets": [
        				"SUGAR",
        				"Ruby"
        			],
        			"score": 2.8,
        			"email": "cecille-whatley84@logical.com",
        			"url": "https://user.com",
        			"description": "bargains independence smell sharing electric extra failures wallpaper freelance higher mathematics disaster directed clicking elder anyone encountered living mattress drill",
        			"verified": true,
        			"salary": 38775
        		},
        		{
        			"_id": "6CEAN1AXU6F666A4",
        			"name": "Delila Pannell",
        			"dob": "2018-02-07",
        			"address": {
        				"street": "3903 Roselands Circle",
        				"town": "Ossett",
        				"postode": "BD48 6ET"
        			},
        			"telephone": "+221-8848-031-951",
        			"pets": [
        				"Tiger",
        				"Stella"
        			],
        			"score": 4.8,
        			"email": "alba_hawthorne@induced.shimabara.nagasaki.jp",
        			"url": "http://www.perfume.kashiwazaki.niigata.jp",
        			"description": "newsletter resources spent psychiatry recycling hospital turtle rocket footage absence pitch authentic interactive fourth slim middle bm mature synthesis recovered",
        			"verified": false,
        			"salary": 38163
        		},
        		{
        			"_id": "QRNEFG1GPFI5PJR9",
        			"name": "Gabriela Conaway",
        			"dob": "2015-07-23",
        			"address": {
        				"street": "4822 Troon Circle",
        				"town": "Swanscombe and Greenhithe",
        				"postode": "M0 3QW"
        			},
        			"telephone": "+46-0620-673-252",
        			"pets": [
        				"Charlie",
        				"Duke"
        			],
        			"score": 3.6,
        			"email": "marvis01@florist.com",
        			"url": "http://sigma.kira.aichi.jp",
        			"description": "distributors desktop istanbul most contemporary fifteen pointing requesting adaptive wearing soft ap alcohol switch retreat blair fee dosage join mars",
        			"verified": true,
        			"salary": 30479
        		},
        		{
        			"_id": "0VUSDEIHR1KAT6ZR",
        			"name": "Leighann Luong",
        			"dob": "2023-03-31",
        			"address": {
        				"street": "9006 Fairbourne Street",
        				"town": "Otley",
        				"postode": "KA3 8JW"
        			},
        			"telephone": "+98-5045-789-362",
        			"pets": [
        				"Angel",
        				"Gus"
        			],
        			"score": 4.1,
        			"email": "melodee8656@mtv.com",
        			"url": "https://mixer.cuiaba.br",
        			"description": "reload alternatively gras centuries math strips strict algorithms ethics taking mj holly rw friday disease chapel pgp helps bundle welcome",
        			"verified": true,
        			"salary": 54210
        		},
        		{
        			"_id": "MQXQR2KFEN97EAO3",
        			"name": "Cherilyn Radford",
        			"dob": "2019-12-08",
        			"address": {
        				"street": "6250 Norman Avenue",
        				"town": "Royal Leamington Spa",
        				"postode": "WA45 4SE"
        			},
        			"telephone": "+358-0845-573-498",
        			"pets": [
        				"Kiki",
        				"Bailey"
        			],
        			"score": 2.2,
        			"email": "hilda.callahan@episode.com",
        			"url": "http://bufing.com",
        			"description": "devil crime does isa rental banks helmet rent artistic jerry purposes delayed failures sao outside loops buses hispanic attorney matthew",
        			"verified": false,
        			"salary": 11434
        		},
        		{
        			"_id": "MKREUCH3SZFPKTJQ",
        			"name": "King Nabors",
        			"dob": "2020-11-17",
        			"address": {
        				"street": "2240 Coldalhurst Road",
        				"town": "Tadley",
        				"postode": "ME6 2ZB"
        			},
        			"telephone": "+53-8837-073-223",
        			"pets": [
        				"Misty",
        				"Buddy"
        			],
        			"score": 8.8,
        			"email": "buddy-fraley6468@yahoo.com",
        			"url": "https://www.guard.com",
        			"description": "rep mod agricultural closing av cv away loop disputes valuation undertaken friday weapon faqs knew ads fascinating administrators permission shortly",
        			"verified": true,
        			"salary": 62582
        		},
        		{
        			"_id": "GU7TYOG0GDJPJI9T",
        			"name": "Rusty Doughty",
        			"dob": "2018-10-30",
        			"address": {
        				"street": "5380 Brantwood Street",
        				"town": "South Benfleet",
        				"postode": "S8 3OT"
        			},
        			"telephone": "+33-8002-990-182",
        			"pets": [
        				"Lilly",
        				"Duke"
        			],
        			"score": 2.5,
        			"email": "jaymie-mejia-rodrigue90@gmail.com",
        			"url": "https://dishes.com",
        			"description": "likely convert tba diameter pressed tooth electron concerns plug blvd javascript dh melbourne recover compile justify d principle banners context",
        			"verified": true,
        			"salary": 16751
        		},
        		{
        			"_id": "6P8MKM7L1G785V1P",
        			"name": "Scotty Freeman",
        			"dob": "2017-12-30",
        			"address": {
        				"street": "0910 Whitfield",
        				"town": "Scone",
        				"postode": "AL7 3BN"
        			},
        			"telephone": "+65-1463-421-043",
        			"pets": [
        				"CoCo",
        				"Bear"
        			],
        			"score": 1.6,
        			"email": "geneva.babb7@livestock.com",
        			"url": "http://www.invitations.com",
        			"description": "bradford substitute majority satisfy outdoors impression licensing treated floppy examined smell trainers adrian legs myth account agrees frame containing gbp",
        			"verified": true,
        			"salary": 67828
        		},
        		{
        			"_id": "QDCYQXXJIVGNS4RU",
        			"name": "Letha Abell",
        			"dob": "2019-09-20",
        			"address": {
        				"street": "7700 Ellenor Circle",
        				"town": "Castleford",
        				"postode": "CT2 8VT"
        			},
        			"telephone": "+62-4664-033-388",
        			"pets": [
        				"Sammy",
        				"Lexi"
        			],
        			"score": 6.5,
        			"email": "theresa497@remove.com",
        			"url": "http://www.da.com",
        			"description": "intl veterans tap tanzania belong warranty crafts consumption posting risk unix tulsa gregory crossing based delete hairy presentations walter newman",
        			"verified": false,
        			"salary": 41762
        		},
        		{
        			"_id": "IQZ878RHHO2X54KN",
        			"name": "Nubia Merriman",
        			"dob": "2021-06-14",
        			"address": {
        				"street": "8913 Runger",
        				"town": "Rackheath",
        				"postode": "CB06 9UJ"
        			},
        			"telephone": "+251-5498-951-264",
        			"pets": [
        				"Callie",
        				"Jack"
        			],
        			"score": 6.7,
        			"email": "charlotteflanagan177@cart.com",
        			"url": "https://hunger.dj",
        			"description": "wayne affiliation days phys trustees pediatric fiji thunder attached possibility tulsa ethernet whilst fight santa merchants fx lu preferred pads",
        			"verified": true,
        			"salary": 63386
        		},
        		{
        			"_id": "2EZUVZQQHGM3ET8E",
        			"name": "Shad Burchfield",
        			"dob": "2018-07-27",
        			"address": {
        				"street": "7288 Lackford Avenue",
        				"town": "Rainham",
        				"postode": "EN0 6UG"
        			},
        			"telephone": "+33-1765-906-835",
        			"pets": [
        				"Peanut",
        				"Oliver"
        			],
        			"score": 6.9,
        			"email": "suzan_foley37491@refresh.com",
        			"url": "https://www.glance.com",
        			"description": "photos soul humanities policies techrepublic estate commodity add media lamb dress testimony authority earliest samba bean bike watt yugoslavia healthy",
        			"verified": true,
        			"salary": 13253
        		},
        		{
        			"_id": "7H72LP449JE0QZVN",
        			"name": "Hyacinth Beard",
        			"dob": "2020-03-26",
        			"address": {
        				"street": "6878 Burstead",
        				"town": "Brentwood",
        				"postode": "S86 2TE"
        			},
        			"telephone": "+36-9587-022-215",
        			"pets": [
        				"Dusty",
        				"Oliver"
        			],
        			"score": 6.3,
        			"email": "sharyl7852@pitch.com",
        			"url": "http://www.guyana.com",
        			"description": "precipitation ladder roommates satin dude celebs fp regards sponsors surprised containing combinations event jr offices bits lakes florist enable establishment",
        			"verified": false,
        			"salary": 27809
        		},
        		{
        			"_id": "AJ3PO7XDAF64S59F",
        			"name": "Nada Hathaway",
        			"dob": "2014-05-11",
        			"address": {
        				"street": "6904 Farnsworth Avenue",
        				"town": "Nottingham",
        				"postode": "HU0 3VF"
        			},
        			"telephone": "+593-1858-838-385",
        			"pets": [
        				"Callie",
        				"Ginger"
        			],
        			"score": 9.6,
        			"email": "brant82413@prescription.com.ni",
        			"url": "https://fireplace.com",
        			"description": "phoenix applicable serial find major guru genre copying expensive o premium li belarus botswana thermal deliver bradley dialog league victorian",
        			"verified": true,
        			"salary": 56699
        		},
        		{
        			"_id": "RDFHOYYIYQEZY9KL",
        			"name": "Kristen Stevens",
        			"dob": "2022-05-18",
        			"address": {
        				"street": "9387 Ena",
        				"town": "Little Coates",
        				"postode": "SW78 0OE"
        			},
        			"telephone": "+39-0641-092-168",
        			"pets": [
        				"Oliver",
        				"Jack"
        			],
        			"score": 8,
        			"email": "dorthea.segura@hotmail.com",
        			"url": "https://loaded.com",
        			"description": "ba offers precious broadcasting invasion victor literally updates accountability aka accomplished builds da cycling oil corruption mesh ja peak jeep",
        			"verified": true,
        			"salary": 37550
        		},
        		{
        			"_id": "8Y19A0D8VYP4VQHL",
        			"name": "Natosha Regan",
        			"dob": "2022-11-21",
        			"address": {
        				"street": "5616 Shirebrook",
        				"town": "Lechlade",
        				"postode": "HG09 7QQ"
        			},
        			"telephone": "+45-5698-414-301",
        			"pets": [
        				"Belle",
        				"Sam"
        			],
        			"score": 3.8,
        			"email": "ezequiel_tiller693@votes.com",
        			"url": "https://www.buyer.com",
        			"description": "align presenting codes contracts zoloft cigarettes impaired dolls linear counseling perhaps circulation das anna adequate funeral broadband sync grad arrival",
        			"verified": true,
        			"salary": 35041
        		},
        		{
        			"_id": "FZXX9S1YEXUE7QXT",
        			"name": "Evangelina Worrell",
        			"dob": "2015-02-23",
        			"address": {
        				"street": "1142 Vantomme Road",
        				"town": "Langley Mill",
        				"postode": "FY5 8PC"
        			},
        			"telephone": "+266-4488-227-054",
        			"pets": [
        				"Rusty",
        				"Dexter"
        			],
        			"score": 8.6,
        			"email": "concha-long10@yahoo.com",
        			"url": "http://www.dan.com",
        			"description": "reef mental angle springs dying shipped minimum princess resume speaker ciao substantial specify deck casting killed stamp bo stages fellowship",
        			"verified": false,
        			"salary": 14350
        		},
        		{
        			"_id": "FOIIP6V5QJ73TGFE",
        			"name": "Mamie Ferreira",
        			"dob": "2015-04-25",
        			"address": {
        				"street": "3916 Wareing Lane",
        				"town": "Comrie",
        				"postode": "CV0 2JN"
        			},
        			"telephone": "+47-0368-483-623",
        			"pets": [
        				"Chloe",
        				"Ruby"
        			],
        			"score": 3.2,
        			"email": "diegolayman@gmail.com",
        			"url": "https://www.executed.com",
        			"description": "puts hello packets az messages pike pam contributors chrysler fp ps monetary berry renewal edt per muslim penalties virginia remaining",
        			"verified": true,
        			"salary": 23964
        		},
        		{
        			"_id": "JY4KQ7ZLP93YBFTG",
        			"name": "Taina Brandenburg",
        			"dob": "2019-04-27",
        			"address": {
        				"street": "7847 Basten",
        				"town": "Stocksbridge",
        				"postode": "TA0 7QB"
        			},
        			"telephone": "+268-1926-217-192",
        			"pets": [
        				"Simba",
        				"Cody"
        			],
        			"score": 6.2,
        			"email": "classie-sage8@manufacturers.com",
        			"url": "https://stayed.com",
        			"description": "genetic begin ia queens adapters uganda owner jonathan retired discount knit authorization certain applicants plugins promotions attention our specs rochester",
        			"verified": true,
        			"salary": 23218
        		},
        		{
        			"_id": "YMIO6BZZ5LTR51TP",
        			"name": "Eun Ward",
        			"dob": "2021-09-06",
        			"address": {
        				"street": "4932 Farrell Avenue",
        				"town": "Dollar",
        				"postode": "DG59 9YW"
        			},
        			"telephone": "+64-2367-673-643",
        			"pets": [
        				"Pumpkin",
        				"Shadow"
        			],
        			"score": 7.7,
        			"email": "selena_wiese22901@gmail.com",
        			"url": "http://www.map.com",
        			"description": "acre charleston louisville colleges beverly por outline japanese hart hp ancient fort bizarre van barbie worse plasma wp belts strength",
        			"verified": false,
        			"salary": 67665
        		},
        		{
        			"_id": "C4U18R49EZIGPB2Z",
        			"name": "Dexter Bonner",
        			"dob": "2015-10-29",
        			"address": {
        				"street": "3864 Rostron Road",
        				"town": "Hatfield",
        				"postode": "GU3 4LQ"
        			},
        			"telephone": "+268-3236-580-437",
        			"pets": [
        				"Peanut",
        				"Ginger"
        			],
        			"score": 1.2,
        			"email": "genie_hendricks979@les.takino.hyogo.jp",
        			"url": "http://www.midi.com",
        			"description": "feeling close mc wayne estimates purpose classic opponent most brothers ham bingo nottingham and adam prerequisite doom besides thailand lane",
        			"verified": true,
        			"salary": 28137
        		},
        		{
        			"_id": "56MXBNM12FATLNVI",
        			"name": "Alvaro Villarreal",
        			"dob": "2023-05-24",
        			"address": {
        				"street": "7364 Lingards Circle",
        				"town": "Patchway",
        				"postode": "WD35 0ZA"
        			},
        			"telephone": "+670-2918-496-652",
        			"pets": [
        				"Dusty",
        				"Buddy"
        			],
        			"score": 5.7,
        			"email": "kathryne_shanks@leisure.com",
        			"url": "http://trying.com",
        			"description": "indians martin exports stomach field chester accounting lu orbit reply saturn viewers begin generating she personnel electrical dp lines light",
        			"verified": true,
        			"salary": 55703
        		},
        		{
        			"_id": "JHAJQ5S0JVPLFQJQ",
        			"name": "Jonas Roberson",
        			"dob": "2015-01-16",
        			"address": {
        				"street": "9460 Greenleach Road",
        				"town": "Bude",
        				"postode": "NN31 9IG"
        			},
        			"telephone": "+506-1959-085-739",
        			"pets": [
        				"Felix",
        				"Teddy"
        			],
        			"score": 1.9,
        			"email": "elina.gallegos386@yahoo.com",
        			"url": "http://www.wu.com",
        			"description": "responsibilities walk commitments equity uc chicken domains lands mrna obtained journals exam tom witnesses xanax aluminum dinner screensavers guild prozac",
        			"verified": true,
        			"salary": 56932
        		},
        		{
        			"_id": "BQ6SOG974MD8N828",
        			"name": "Barbra Kenney",
        			"dob": "2016-12-28",
        			"address": {
        				"street": "2919 Viewlands Road",
        				"town": "Worsley",
        				"postode": "G02 9PE"
        			},
        			"telephone": "+591-9283-059-613",
        			"pets": [
        				"Baby",
        				"Emma"
        			],
        			"score": 8.1,
        			"email": "cheryll_sallee@yahoo.com",
        			"url": "http://www.sorted.com",
        			"description": "luis situations provision customized similarly previously wisconsin mounting sessions sport partners corporations creation j gender floating like lowest genetics consistently",
        			"verified": false,
        			"salary": 66137
        		},
        		{
        			"_id": "2C2K6K4AEELQ2FG6",
        			"name": "Julia Tenney",
        			"dob": "2014-11-09",
        			"address": {
        				"street": "0568 Fairlands Road",
        				"town": "Dunwich",
        				"postode": "EH87 9DY"
        			},
        			"telephone": "+351-0880-127-685",
        			"pets": [
        				"Dexter",
        				"Stella"
        			],
        			"score": 4.1,
        			"email": "ilse.solomon6799@marriage.us-east-1.amazonaws.com",
        			"url": "https://www.expiration.com",
        			"description": "arising longitude theme radio flexibility informational geek wealth sister hospital racks feat chemistry cars mod wheat futures gcc belle onto",
        			"verified": true,
        			"salary": 42470
        		},
        		{
        			"_id": "QA59QZ78Q6OOXAXN",
        			"name": "Chara Broughton",
        			"dob": "2020-02-15",
        			"address": {
        				"street": "0949 Crossley",
        				"town": "Garforth",
        				"postode": "HS9 7TD"
        			},
        			"telephone": "+36-0863-877-518",
        			"pets": [
        				"Luna",
        				"Max"
        			],
        			"score": 1,
        			"email": "titus-tibbetts110@gmail.com",
        			"url": "http://www.closest.com",
        			"description": "tracker spots ram and vancouver robin gary disciplinary practitioner ka negotiation considerations holders arm july signal lack knives queue bond",
        			"verified": true,
        			"salary": 13084
        		},
        		{
        			"_id": "DT6IX195MKMD8S17",
        			"name": "Vasiliki Ely-Archuleta",
        			"dob": "2018-04-16",
        			"address": {
        				"street": "1188 Manston Circle",
        				"town": "Lincoln",
        				"postode": "DY2 2UG"
        			},
        			"telephone": "+962-2391-907-621",
        			"pets": [
        				"Toby",
        				"Marley"
        			],
        			"score": 3.4,
        			"email": "hans631@gmail.com",
        			"url": "http://www.belly.com",
        			"description": "memo lost brunswick teaches screenshot tail potato overview commitment batman railway jul affecting ace bristol thrown taste dedicated turkey crimes",
        			"verified": false,
        			"salary": 68202
        		},
        		{
        			"_id": "ATQ4ZZPGZFJGQ9TU",
        			"name": "Ashlee Finch",
        			"dob": "2017-07-25",
        			"address": {
        				"street": "7187 Lingmoor",
        				"town": "Old Colwyn",
        				"postode": "SO9 3SJ"
        			},
        			"telephone": "+33-8497-151-774",
        			"pets": [
        				"Cleo",
        				"Jax"
        			],
        			"score": 5.8,
        			"email": "shakiabohannon3250@knights.mil.qa",
        			"url": "https://logan.com",
        			"description": "dramatic leather subscribe joins alabama service partly laser arbitration parliament hughes councils suites advert sucking extraction islam essentials born insulation",
        			"verified": false,
        			"salary": 32582
        		},
        		{
        			"_id": "R0DVSIYMGEE3JT1K",
        			"name": "Lecia Causey",
        			"dob": "2014-12-19",
        			"address": {
        				"street": "7421 Gray Avenue",
        				"town": "Winchelsea",
        				"postode": "WF74 3CW"
        			},
        			"telephone": "+53-8245-701-609",
        			"pets": [
        				"sox",
        				"Jake"
        			],
        			"score": 10,
        			"email": "mavis6493@granny.com",
        			"url": "https://www.engagement.com",
        			"description": "stuffed theaters ultimate tactics intimate morris roots hawaiian maiden lawrence theaters sample rapids gp prefer teaching mount phillips anatomy districts",
        			"verified": true,
        			"salary": 27840
        		},
        		{
        			"_id": "YN0E0M3N1JU1Q4BS",
        			"name": "Margeret Michaud",
        			"dob": "2020-10-22",
        			"address": {
        				"street": "7693 Woodlea",
        				"town": "Dagenham",
        				"postode": "CB2 8AI"
        			},
        			"telephone": "+502-5878-681-419",
        			"pets": [
        				"Lily",
        				"Bear"
        			],
        			"score": 1.2,
        			"email": "merlene-biddle9@ind.pro.mv",
        			"url": "https://www.cleaning.beskidy.pl",
        			"description": "includes alike death disco route consequence polls dam sight stanford workstation amy attorneys lies creator charms stockholm credit singing inexpensive",
        			"verified": false,
        			"salary": 68543
        		},
        		{
        			"_id": "TJHT99FASP2Q2ES4",
        			"name": "Felice Glaze",
        			"dob": "2023-02-15",
        			"address": {
        				"street": "3623 Selstead Road",
        				"town": "Cockermouth",
        				"postode": "RM35 4WF"
        			},
        			"telephone": "+53-1387-463-684",
        			"pets": [
        				"Sebastian",
        				"Emma"
        			],
        			"score": 3.9,
        			"email": "deshawnbeckett46353@gmail.com",
        			"url": "http://applicant.com",
        			"description": "executive ex fc trouble celtic pamela different repair parker warrior arkansas expires spanish authorities representation template partial flexible pass briefing",
        			"verified": false,
        			"salary": 21742
        		},
        		{
        			"_id": "0JYP33K2BSY0F4MK",
        			"name": "Isela Tabor-Norris",
        			"dob": "2021-09-26",
        			"address": {
        				"street": "3362 Vickers Avenue",
        				"town": "Jarrow",
        				"postode": "DT4 0MD"
        			},
        			"telephone": "+61-3637-045-692",
        			"pets": [
        				"CoCo",
        				"Jax"
        			],
        			"score": 2,
        			"email": "robby-yates@yahoo.com",
        			"url": "http://examinations.com",
        			"description": "telling jamie dish witnesses you riders rent approach astrology traditions runner decent heater heights disks porter jason rv chemicals lancaster",
        			"verified": true,
        			"salary": 49587
        		},
        		{
        			"_id": "YSA4H3D79VZH2Q1I",
        			"name": "Riley Pratt",
        			"dob": "2021-04-10",
        			"address": {
        				"street": "9029 Carnoustie Street",
        				"town": "Oundle",
        				"postode": "TW2 2MV"
        			},
        			"telephone": "+66-1890-697-938",
        			"pets": [
        				"Pepper",
        				"Apollo"
        			],
        			"score": 9.7,
        			"email": "annabelle-nelson-sanders9984@effects.com",
        			"url": "http://swiss.com",
        			"description": "inline handled president pot mineral media channel mobiles switched coffee sister handled vs laughing suse whats cards papers nebraska ave",
        			"verified": false,
        			"salary": 43774
        		},
        		{
        			"_id": "C2JRIC5XMV2XQDYN",
        			"name": "Akilah Culpepper",
        			"dob": "2014-11-13",
        			"address": {
        				"street": "4687 Teneriffe Street",
        				"town": "Penwortham",
        				"postode": "KT20 4SK"
        			},
        			"telephone": "+965-0776-334-101",
        			"pets": [
        				"Noodle",
        				"Dexter"
        			],
        			"score": 6.5,
        			"email": "darren9@gmail.com",
        			"url": "http://came.com",
        			"description": "bored records chinese failure temperatures nature reality websites assigned florist passport deployment actions promotion sources force dp atmosphere olive genesis",
        			"verified": true,
        			"salary": 55373
        		},
        		{
        			"_id": "5ZT6VBJD4862OVQC",
        			"name": "Kristina Stover",
        			"dob": "2021-09-13",
        			"address": {
        				"street": "7897 Ludgate Avenue",
        				"town": "Haddington",
        				"postode": "HX5 9YD"
        			},
        			"telephone": "+66-5753-813-686",
        			"pets": [
        				"Frankie",
        				"Ginger"
        			],
        			"score": 2.2,
        			"email": "nannie.whatley475@hotmail.com",
        			"url": "http://suitable.com",
        			"description": "bold ipaq proprietary relax suitable superior rep pose dan indexed texts joseph gods cornell unity remarkable cohen grounds millions fail",
        			"verified": true,
        			"salary": 20746
        		},
        		{
        			"_id": "NIVBMYQIJE0JHQE3",
        			"name": "Ivory Hager",
        			"dob": "2018-09-19",
        			"address": {
        				"street": "0900 Partington Street",
        				"town": "St Clears",
        				"postode": "NG45 9YY"
        			},
        			"telephone": "+973-2068-107-573",
        			"pets": [
        				"Jasper",
        				"Stella"
        			],
        			"score": 2.3,
        			"email": "kaceynobles@suppose.com",
        			"url": "https://probability.com",
        			"description": "portions neighbor delicious generate simulations ohio fa ferrari directive electricity princess gotta temporal consistent relying soon moms registration bolivia suburban",
        			"verified": true,
        			"salary": 35154
        		},
        		{
        			"_id": "8648RK4LCOKYFAHN",
        			"name": "Keesha Spearman",
        			"dob": "2020-01-27",
        			"address": {
        				"street": "2283 Marchwood",
        				"town": "Portishead and North Weston",
        				"postode": "DG91 1CF"
        			},
        			"telephone": "+266-9212-632-553",
        			"pets": [
        				"mittens",
        				"Ellie"
        			],
        			"score": 4.2,
        			"email": "hettie1749@text.com",
        			"url": "http://www.controlling.com",
        			"description": "displays aim shoot strategy anything affairs wb subsection investigation focusing pearl replace recovered would seventh ink land bishop demonstrates authorization",
        			"verified": true,
        			"salary": 23874
        		},
        		{
        			"_id": "0BEFY8L7FRJBDSUU",
        			"name": "Nola Heim",
        			"dob": "2017-01-21",
        			"address": {
        				"street": "9127 Westmorland Circle",
        				"town": "Conwy",
        				"postode": "SG8 7MP"
        			},
        			"telephone": "+671-0294-820-870",
        			"pets": [
        				"boo",
        				"Lilly"
        			],
        			"score": 6.3,
        			"email": "justina40@raising.com",
        			"url": "https://customized.com",
        			"description": "analysis spain moving contacts kay assembled millions weak athens kernel bicycle ds upset memorabilia crew bradley tight disability projects newest",
        			"verified": true,
        			"salary": 38215
        		},
        		{
        			"_id": "QQEYFJD6OSTQBEKY",
        			"name": "Kimbery Kim",
        			"dob": "2019-09-16",
        			"address": {
        				"street": "3064 Kingston Street",
        				"town": "Yeadon",
        				"postode": "SS7 8UO"
        			},
        			"telephone": "+92-3678-298-125",
        			"pets": [
        				"Murphy",
        				"Bear"
        			],
        			"score": 4.7,
        			"email": "rettabowden8400@gmail.com",
        			"url": "https://mustang.com",
        			"description": "keen occurring chick optical shipping somerset techno deer discrimination anchor ot ears maker picnic wrap flour addition huge advert reports",
        			"verified": true,
        			"salary": 41554
        		},
        		{
        			"_id": "YM36I083BP15HA4V",
        			"name": "Francesca Wicks",
        			"dob": "2017-03-05",
        			"address": {
        				"street": "8737 Chetwyn Circle",
        				"town": "Inverness",
        				"postode": "G9 6YX"
        			},
        			"telephone": "+34-4181-651-414",
        			"pets": [
        				"Sophie",
        				"Dexter"
        			],
        			"score": 3.7,
        			"email": "vitobeeson6181@yahoo.com",
        			"url": "https://www.pamela.com",
        			"description": "sewing civic itself circle pottery biology trinidad printer often task ye mhz plot performing land radius swim rack wb plugin",
        			"verified": false,
        			"salary": 58615
        		},
        		{
        			"_id": "QQHAQITNR2VSX33Q",
        			"name": "Ilene Ames",
        			"dob": "2017-07-10",
        			"address": {
        				"street": "7598 Booths Road",
        				"town": "Featherstone",
        				"postode": "DE07 5UX"
        			},
        			"telephone": "+66-4042-102-932",
        			"pets": [
        				"tucker",
        				"Marley"
        			],
        			"score": 5.7,
        			"email": "sherrill59@hotmail.com",
        			"url": "http://knee.com",
        			"description": "copies guilty therapy ghana join skip anyone incidence point website levels modular nikon beverly vietnam dynamics tu wrist founder structured",
        			"verified": true,
        			"salary": 66294
        		},
        		{
        			"_id": "Q6SV56K6UGKAPQM3",
        			"name": "Lakeshia Martindale",
        			"dob": "2020-02-28",
        			"address": {
        				"street": "1985 Ashley Avenue",
        				"town": "Redditch",
        				"postode": "WD1 2MZ"
        			},
        			"telephone": "+503-0760-223-380",
        			"pets": [
        				"Lola",
        				"Mia"
        			],
        			"score": 9.2,
        			"email": "jeanie_weeks72041@hotmail.com",
        			"url": "https://ep.com",
        			"description": "expect reaction holly remark jason download matters stockholm completed laughing fcc mobiles trains baseline crossword yn il shanghai closes indonesian",
        			"verified": true,
        			"salary": 16869
        		},
        		{
        			"_id": "02BIDLJG194QQI9Y",
        			"name": "Ryan Washington",
        			"dob": "2020-11-25",
        			"address": {
        				"street": "1726 Balliol",
        				"town": "Whitstable",
        				"postode": "IV6 2IC"
        			},
        			"telephone": "+592-6275-136-493",
        			"pets": [
        				"Boots",
        				"Zeus"
        			],
        			"score": 9.9,
        			"email": "angila.winchester2135@hotmail.com",
        			"url": "http://www.dip.com",
        			"description": "maine vatican cry romantic recycling mazda ronald double designers chubby moscow synthetic administrative fleece workplace dell freebsd gentle ep baking",
        			"verified": true,
        			"salary": 53312
        		},
        		{
        			"_id": "A8ZYBQX02O4622AV",
        			"name": "Zachariah Carnes",
        			"dob": "2021-09-15",
        			"address": {
        				"street": "4283 Bridson Avenue",
        				"town": "Falmouth",
        				"postode": "EC79 6PE"
        			},
        			"telephone": "+33-9740-058-093",
        			"pets": [
        				"Max",
        				"Buddy"
        			],
        			"score": 1,
        			"email": "cary_martel@follows.com",
        			"url": "https://class.com",
        			"description": "eligibility advancement football refers typical sonic cuba calm pacific wow park extra bat viruses educated offices sony deficit lat radical",
        			"verified": true,
        			"salary": 46705
        		},
        		{
        			"_id": "S72XP117QTS9PGXL",
        			"name": "Kraig Hayden",
        			"dob": "2018-11-24",
        			"address": {
        				"street": "9592 Darwen Road",
        				"town": "Wotton under Edge",
        				"postode": "WN45 1MN"
        			},
        			"telephone": "+350-8288-714-892",
        			"pets": [
        				"Princess",
        				"Nala"
        			],
        			"score": 7.6,
        			"email": "thaddeus87417@gmail.com",
        			"url": "http://seats.com",
        			"description": "nba depression spirits henderson individual removable cn database tissue cinema consultants dock around usr bones attached emerging payable index group",
        			"verified": true,
        			"salary": 16004
        		},
        		{
        			"_id": "K36SZLAATH9U32E2",
        			"name": "Brooke Hyde",
        			"dob": "2021-04-13",
        			"address": {
        				"street": "8850 Crofton Circle",
        				"town": "Portrush",
        				"postode": "TR8 9JG"
        			},
        			"telephone": "+39-3139-767-272",
        			"pets": [
        				"Misty",
        				"Rosie"
        			],
        			"score": 2.3,
        			"email": "barry336@gmail.com",
        			"url": "https://pleased.com",
        			"description": "pics exceptional covers produces flex net cached raid startup mattress artist locked newer weekend reset infectious sit livecam percent hosting",
        			"verified": true,
        			"salary": 29650
        		},
        		{
        			"_id": "3RM3XBQ0S5YMRRPY",
        			"name": "Ilona Gantt",
        			"dob": "2021-05-01",
        			"address": {
        				"street": "0799 Eden Road",
        				"town": "Hayle",
        				"postode": "LN03 2OS"
        			},
        			"telephone": "+82-0237-770-488",
        			"pets": [
        				"Max",
        				"Lilly"
        			],
        			"score": 1.8,
        			"email": "shirley.mattox14332@yahoo.com",
        			"url": "http://www.condos.com",
        			"description": "teeth gallery asking reads sa faith right seasons farms respected automotive examples tour mood anderson scary sites clone grow sisters",
        			"verified": false,
        			"salary": 67087
        		},
        		{
        			"_id": "OYPLI143FY2DATHU",
        			"name": "Gustavo Newberry",
        			"dob": "2017-02-14",
        			"address": {
        				"street": "5004 Winifred Street",
        				"town": "Littlehampton",
        				"postode": "GL45 3AF"
        			},
        			"telephone": "+260-1892-979-433",
        			"pets": [
        				"Boots",
        				"Shadow"
        			],
        			"score": 1.6,
        			"email": "norma3@somerset.com",
        			"url": "http://pair.com",
        			"description": "virgin thing dicke alexander powerpoint recordings specific spoken cases fitness enjoyed cloth ends scott coordinated beginning magnetic behind gives generations",
        			"verified": true,
        			"salary": 40316
        		},
        		{
        			"_id": "7BH73QUUGQEQMKPY",
        			"name": "Roscoe Seeley",
        			"dob": "2016-05-06",
        			"address": {
        				"street": "4591 Brackenhurst Circle",
        				"town": "Lytham St Annes",
        				"postode": "GL17 8EX"
        			},
        			"telephone": "+231-3940-507-401",
        			"pets": [
        				"Lilly",
        				"Cody"
        			],
        			"score": 4.5,
        			"email": "latia15931@syndication.sondrio.it",
        			"url": "http://beam.com",
        			"description": "mayor holland disputes entering barbados monsters receiver electoral banana oldest ships your copying comfortable comparative knowledge june retain gear nbc",
        			"verified": true,
        			"salary": 27929
        		},
        		{
        			"_id": "QLZM37KZGMCIQMPL",
        			"name": "Tuan Bratton",
        			"dob": "2021-04-24",
        			"address": {
        				"street": "8073 Stretford",
        				"town": "Dumbarton",
        				"postode": "HX5 2KI"
        			},
        			"telephone": "+45-5887-998-306",
        			"pets": [
        				"Misty",
        				"Harley"
        			],
        			"score": 9.6,
        			"email": "carleen.treat@gmail.com",
        			"url": "http://www.engaged.com",
        			"description": "tier foods completely phil shaft follow shelter offline extraction membrane hop super creations medicine photos sleeping intense ht excitement instruction",
        			"verified": true,
        			"salary": 50465
        		},
        		{
        			"_id": "84TJD43FO2OYD4GO",
        			"name": "Bridgette Trent",
        			"dob": "2023-12-04",
        			"address": {
        				"street": "0087 Cromdale Lane",
        				"town": "Portsmouth",
        				"postode": "PO58 1WY"
        			},
        			"telephone": "+46-3453-741-464",
        			"pets": [
        				"Fiona",
        				"Sasha"
        			],
        			"score": 7.1,
        			"email": "elois.hoppe@provinces.trentino-altoadige.it",
        			"url": "http://examining.com",
        			"description": "cosmetic alt mysql give testing implement reception forward synthetic creature trail adsl languages trauma greek twenty defendant served blind jpeg",
        			"verified": false,
        			"salary": 47734
        		},
        		{
        			"_id": "7A10CRNKUJZ0NKJ1",
        			"name": "Jeanelle Brower",
        			"dob": "2016-05-21",
        			"address": {
        				"street": "4916 Blackwood",
        				"town": "Stroud",
        				"postode": "B00 0IM"
        			},
        			"telephone": "+592-5201-551-021",
        			"pets": [
        				"Rusty",
        				"Apollo"
        			],
        			"score": 3.5,
        			"email": "sadielandry@brook.com",
        			"url": "https://www.ross.com",
        			"description": "who ef answered street rotation boats podcast governance rehabilitation consultation while spirituality pencil one press person prior elected most ver",
        			"verified": true,
        			"salary": 27595
        		},
        		{
        			"_id": "OAS78JF34Z1FVP7G",
        			"name": "Roxane Sierra",
        			"dob": "2015-10-07",
        			"address": {
        				"street": "9624 Hollins Street",
        				"town": "North Shields",
        				"postode": "NW8 4SK"
        			},
        			"telephone": "+358-0580-708-034",
        			"pets": [
        				"Scooter",
        				"Buddy"
        			],
        			"score": 8.1,
        			"email": "eusebiarivero7653@yahoo.com",
        			"url": "https://www.shipped.com",
        			"description": "collect addresses someone generating powers heavily utc annually covered trail qualifying fl participated logged hp g scientific coffee ml tough",
        			"verified": false,
        			"salary": 44784
        		},
        		{
        			"_id": "PJZIKBB5UGQY680G",
        			"name": "Rudolph Johns",
        			"dob": "2020-08-22",
        			"address": {
        				"street": "7398 Back",
        				"town": "Duniplace",
        				"postode": "KT29 8DE"
        			},
        			"telephone": "+90-1120-937-946",
        			"pets": [
        				"Milo",
        				"Nala"
        			],
        			"score": 8,
        			"email": "remona.saucedo@holding.com",
        			"url": "http://subject.com",
        			"description": "influence idol forgot suspended grown concern zu children comment webpage breeds canon talking civil basically privilege boulder speed vegetarian eve",
        			"verified": true,
        			"salary": 64551
        		},
        		{
        			"_id": "FJ1QU3Y2RHS20ZM4",
        			"name": "Letty Davenport",
        			"dob": "2018-04-11",
        			"address": {
        				"street": "2546 Owlwood Road",
        				"town": "Saltburn by the Sea",
        				"postode": "HU03 3ZR"
        			},
        			"telephone": "+886-9362-087-451",
        			"pets": [
        				"Maggie",
        				"Mia"
        			],
        			"score": 5.4,
        			"email": "betsy-boles45413@absence.com",
        			"url": "https://www.education.com",
        			"description": "mic mcdonald requiring macromedia fonts tribute rack substances dictionary release para blog section diversity processing leu avoid special line nextel",
        			"verified": false,
        			"salary": 51990
        		},
        		{
        			"_id": "LN68OIVNI8YEHBR1",
        			"name": "Neida Damico",
        			"dob": "2018-11-22",
        			"address": {
        				"street": "7796 Ladyshore Street",
        				"town": "Fenny Stratford",
        				"postode": "CO05 3VD"
        			},
        			"telephone": "+260-2845-548-530",
        			"pets": [
        				"Luna",
        				"Sam"
        			],
        			"score": 8.5,
        			"email": "kanesha98@engine.com",
        			"url": "http://www.optimum.com",
        			"description": "ca circle superintendent distributor interactive civilization background molecules mp stripes reproduction calculated flux able us neighborhood russell suffered stored marble",
        			"verified": true,
        			"salary": 40763
        		},
        		{
        			"_id": "PK4EGSSL903PO18O",
        			"name": "Santana Augustine",
        			"dob": "2018-10-31",
        			"address": {
        				"street": "5476 Burran Road",
        				"town": "Aldridge",
        				"postode": "NR19 3JT"
        			},
        			"telephone": "+42-8959-252-781",
        			"pets": [
        				"Tiger",
        				"Bentley"
        			],
        			"score": 2,
        			"email": "tuansummerlin@yahoo.com",
        			"url": "https://accused.com",
        			"description": "establish valued oo eagles qc blanket packages owns sand inspector theme potter gain need dan alerts exam epson ordinance incentives",
        			"verified": true,
        			"salary": 20832
        		},
        		{
        			"_id": "QRDQ5M6MA06ABIRO",
        			"name": "Rico Baum",
        			"dob": "2018-04-15",
        			"address": {
        				"street": "3533 Rowton",
        				"town": "Weston super Mare",
        				"postode": "B10 2UH"
        			},
        			"telephone": "+505-1339-333-063",
        			"pets": [
        				"George",
        				"Duke"
        			],
        			"score": 1.4,
        			"email": "maragret_stowe253@criticism.com",
        			"url": "https://carter.com",
        			"description": "thumbnail broken ppc delight scenario cute ant bit sig requiring quoted rebel typical brush mission zealand map clouds elimination meetings",
        			"verified": true,
        			"salary": 55113
        		},
        		{
        			"_id": "Y2ZZSQ1SQ4H96ODM",
        			"name": "Lottie Hurt",
        			"dob": "2023-05-31",
        			"address": {
        				"street": "9896 Bulteel Street",
        				"town": "Dingwall",
        				"postode": "BS65 8AT"
        			},
        			"telephone": "+675-1540-885-858",
        			"pets": [
        				"MIMI",
        				"Jake"
        			],
        			"score": 2.7,
        			"email": "jamika3@symptoms.com",
        			"url": "https://belarus.com",
        			"description": "planets nm divorce handles constitutional taxi cruz zdnet update selected complexity fiber ricky lingerie outlined spanking lover disabilities supreme advocate",
        			"verified": false,
        			"salary": 21390
        		},
        		{
        			"_id": "6IXCNIAPSLFB30L9",
        			"name": "Elsy Armenta",
        			"dob": "2022-11-17",
        			"address": {
        				"street": "2003 Crescent Road",
        				"town": "Ashby Woulds",
        				"postode": "FK70 8PJ"
        			},
        			"telephone": "+351-8475-596-127",
        			"pets": [
        				"Garfield",
        				"Roxy"
        			],
        			"score": 5.1,
        			"email": "marion-jordan804@happen.com",
        			"url": "http://ot.com",
        			"description": "export ac loved invision controversial indians blast pichunter process iraq named pr flyer acquisition gym am trouble kitty fd hook",
        			"verified": true,
        			"salary": 57454
        		},
        		{
        			"_id": "FG24QMXTPQH6G6JG",
        			"name": "Sheridan Lockwood",
        			"dob": "2016-07-27",
        			"address": {
        				"street": "6625 Sulgrave Lane",
        				"town": "Earley",
        				"postode": "LL20 6ME"
        			},
        			"telephone": "+225-8025-293-370",
        			"pets": [
        				"Sadie",
        				"Buddy"
        			],
        			"score": 3,
        			"email": "remona-windsor4089@barry.com",
        			"url": "http://www.vegetarian.com",
        			"description": "appointments arrested hop ruling packing assets jesse alloy assets fastest mothers determination mac appeared min src distance seems previously nickname",
        			"verified": false,
        			"salary": 66568
        		},
        		{
        			"_id": "937QER84SCCVFYLD",
        			"name": "Ginette Nagel",
        			"dob": "2022-10-11",
        			"address": {
        				"street": "7678 Everleigh Road",
        				"town": "South Benfleet",
        				"postode": "IP01 3PD"
        			},
        			"telephone": "+90-8897-278-589",
        			"pets": [
        				"George",
        				"Murphy"
        			],
        			"score": 9.5,
        			"email": "danyel.kenney@yahoo.com",
        			"url": "http://www.anti.gov.sc",
        			"description": "collaboration alto lost cab robust elections cleared respectively funeral food loss extra protocols communication colours cruz open approval cut strikes",
        			"verified": true,
        			"salary": 10353
        		},
        		{
        			"_id": "60XY943Q3LQ01F63",
        			"name": "Phyliss Winkler",
        			"dob": "2014-08-30",
        			"address": {
        				"street": "1049 Gould",
        				"town": "Hornsea",
        				"postode": "WV19 0SM"
        			},
        			"telephone": "+260-0804-882-014",
        			"pets": [
        				"Leo",
        				"Bailey"
        			],
        			"score": 5.9,
        			"email": "sharondareiss@sure.com",
        			"url": "http://www.intensive.com",
        			"description": "saskatchewan being pour kingston obj vb ceiling racing av increases attitude craig ag uniprotkb fill steven myspace christians al questionnaire",
        			"verified": true,
        			"salary": 60581
        		},
        		{
        			"_id": "PAJLBFMPDJC9YPR5",
        			"name": "Una Bowden",
        			"dob": "2018-03-13",
        			"address": {
        				"street": "1586 Grave Lane",
        				"town": "Motherwell",
        				"postode": "SG6 0TI"
        			},
        			"telephone": "+971-1008-527-355",
        			"pets": [
        				"Rusty",
        				"Scout"
        			],
        			"score": 8.3,
        			"email": "mason59@gmail.com",
        			"url": "https://change.gildeskål.no",
        			"description": "efficiency odd essex ship zinc papers payday manitoba recycling against moore borders pit invasion lighting feels activists preference parks appointments",
        			"verified": true,
        			"salary": 67484
        		},
        		{
        			"_id": "T0JCRHV90E41SS91",
        			"name": "Nickolas Savage",
        			"dob": "2017-08-21",
        			"address": {
        				"street": "0727 Lymmington Street",
        				"town": "Kidwelly",
        				"postode": "RM2 0BT"
        			},
        			"telephone": "+255-8832-671-094",
        			"pets": [
        				"Misty",
        				"Henry"
        			],
        			"score": 3.6,
        			"email": "aretha9@miscellaneous.stream",
        			"url": "https://www.selling.com",
        			"description": "social thats differences platform quoted release grocery copies traditions header vault accommodations vc therapeutic king caution gp connectivity dividend hook",
        			"verified": true,
        			"salary": 24303
        		},
        		{
        			"_id": "2ACDO84RDYZFCSML",
        			"name": "Hwa Frey",
        			"dob": "2023-03-09",
        			"address": {
        				"street": "7236 Riverside Lane",
        				"town": "Leyton",
        				"postode": "SS75 0AP"
        			},
        			"telephone": "+45-8150-396-957",
        			"pets": [
        				"cupcake",
        				"Jax"
        			],
        			"score": 5.6,
        			"email": "ashli_berman@pty.com",
        			"url": "https://www.bhutan.com",
        			"description": "denial merchant than chips metadata weights drugs followed jesus ix edt geek establishment construct owen followed sensor bull s said",
        			"verified": true,
        			"salary": 28236
        		},
        		{
        			"_id": "AVGOBIJ6M0KB5QEH",
        			"name": "Ismael Duval",
        			"dob": "2017-09-30",
        			"address": {
        				"street": "5732 Meadway Avenue",
        				"town": "Mildenhall",
        				"postode": "SO9 8QG"
        			},
        			"telephone": "+231-4889-238-803",
        			"pets": [
        				"Angel",
        				"Lexi"
        			],
        			"score": 1,
        			"email": "brande866@wisdom.com",
        			"url": "http://administered.com",
        			"description": "nancy aggregate japan genius portion tender replacement kruger will ultimate survivors sucking coordinated arizona hayes developments indication manager suit qc",
        			"verified": true,
        			"salary": 13366
        		},
        		{
        			"_id": "L72D6Y3T8QV5ICV7",
        			"name": "Rebeca Pomeroy",
        			"dob": "2016-01-18",
        			"address": {
        				"street": "8651 Whiteway Street",
        				"town": "Swanscombe and Greenhithe",
        				"postode": "HR07 9ID"
        			},
        			"telephone": "+225-9552-254-977",
        			"pets": [
        				"Toby",
        				"Murphy"
        			],
        			"score": 5.9,
        			"email": "tatyana.hawthorne@yoga.com",
        			"url": "http://www.intimate.com",
        			"description": "violation lite summer visiting love priority mirrors refuse depression launched routes cable borders appreciation beverage hrs believes ni threat proper",
        			"verified": false,
        			"salary": 51426
        		},
        		{
        			"_id": "AAIH840SAXYFSUZX",
        			"name": "Martha Holloway",
        			"dob": "2016-06-28",
        			"address": {
        				"street": "2757 Wheatley Circle",
        				"town": "Wirksworth",
        				"postode": "BT3 9XJ"
        			},
        			"telephone": "+218-8275-835-792",
        			"pets": [
        				"Ginger",
        				"Ruby"
        			],
        			"score": 8.3,
        			"email": "velva5437@yahoo.com",
        			"url": "https://www.dee.com",
        			"description": "reproductive disaster publishing source gamecube relocation hong climb db weblog mine taught norfolk toe routes assault casey blink direct circular",
        			"verified": false,
        			"salary": 39329
        		},
        		{
        			"_id": "6KYEPTV9M2KYGSHH",
        			"name": "Mireya Carden",
        			"dob": "2018-01-03",
        			"address": {
        				"street": "7539 Walmsley Avenue",
        				"town": "Kirkwall",
        				"postode": "BB7 8OG"
        			},
        			"telephone": "+213-5408-612-852",
        			"pets": [
        				"Gizmo",
        				"Riley"
        			],
        			"score": 3,
        			"email": "kristine_hoskins37482@gmail.com",
        			"url": "http://www.supply.com",
        			"description": "sticks lesser lat sharon safe differently misc glenn forbidden listings schema unavailable slovak generating pharmacies packet tent practices governmental avg",
        			"verified": false,
        			"salary": 29502
        		},
        		{
        			"_id": "LNVQMDCMSQLSX9D2",
        			"name": "Angeline Chow-Hutchins",
        			"dob": "2014-07-06",
        			"address": {
        				"street": "0932 Haslam Avenue",
        				"town": "Cuckfield",
        				"postode": "M7 2JV"
        			},
        			"telephone": "+64-2341-241-263",
        			"pets": [
        				"Scooter",
        				"Lilly"
        			],
        			"score": 3.1,
        			"email": "joy3181@slight.com",
        			"url": "http://www.teaching.com",
        			"description": "cardiff deputy abandoned minor mug mission demand rhythm celebs island larger informed likelihood egypt eminem passage bang park par sizes",
        			"verified": false,
        			"salary": 58720
        		},
        		{
        			"_id": "Q1SQ5RKZFA713LU6",
        			"name": "Fern Hildebrand",
        			"dob": "2015-08-09",
        			"address": {
        				"street": "3049 Cudworth",
        				"town": "Fairfield",
        				"postode": "N34 5TJ"
        			},
        			"telephone": "+598-0743-342-958",
        			"pets": [
        				"Patches",
        				"Bailey"
        			],
        			"score": 6.8,
        			"email": "adeline.salerno@hotmail.com",
        			"url": "http://dump.com",
        			"description": "rainbow conditional managers karl entry buck theaters huge ko excluded ross mpeg torture attractions americas cf tags hairy expert spine",
        			"verified": false,
        			"salary": 18501
        		},
        		{
        			"_id": "7VY3LDI1X34UL4BL",
        			"name": "Antonetta Hoppe",
        			"dob": "2021-11-18",
        			"address": {
        				"street": "2542 Barrow Lane",
        				"town": "Minehead",
        				"postode": "N6 2VC"
        			},
        			"telephone": "+241-8950-956-345",
        			"pets": [
        				"Garfield",
        				"Murphy"
        			],
        			"score": 4.9,
        			"email": "bella-carney@meat.com",
        			"url": "http://promoting.com",
        			"description": "catch blues savage agreements villages motorola regulations followed penalties motel demonstrates silk hang mpg monsters element flows bills render offshore",
        			"verified": false,
        			"salary": 49645
        		},
        		{
        			"_id": "6TOP00JX1UF5BSQG",
        			"name": "Lida Storm",
        			"dob": "2019-01-24",
        			"address": {
        				"street": "5866 Oliver Street",
        				"town": "Hythe",
        				"postode": "HS72 3LV"
        			},
        			"telephone": "+213-8804-176-192",
        			"pets": [
        				"Rocky",
        				"Max"
        			],
        			"score": 8,
        			"email": "lelah_yamamoto@packing.com",
        			"url": "http://www.must.com",
        			"description": "occupied sales musicians calculation formed publication documented naval story africa recreation focal courage persian camel countries synthesis naturally became raising",
        			"verified": true,
        			"salary": 53855
        		},
        		{
        			"_id": "J2HBAGOMY0F0UG3Q",
        			"name": "Tammi Hoyle",
        			"dob": "2018-02-09",
        			"address": {
        				"street": "6495 Crossefield Circle",
        				"town": "Loddon",
        				"postode": "CV80 1UX"
        			},
        			"telephone": "+61-8774-041-849",
        			"pets": [
        				"Pepper",
        				"Penny"
        			],
        			"score": 9.3,
        			"email": "cortezkearney93@yahoo.com",
        			"url": "http://payment.com",
        			"description": "debian eco andrews med sys licenses decreased infants vacuum adidas finds allowing td diff letting charity fixtures name attachment intervention",
        			"verified": true,
        			"salary": 47522
        		},
        		{
        			"_id": "MFIVKBQX1FI6J2VA",
        			"name": "Gabriela Stock",
        			"dob": "2016-09-04",
        			"address": {
        				"street": "8938 Butterhouse Avenue",
        				"town": "Edgware",
        				"postode": "TQ3 4MC"
        			},
        			"telephone": "+599-6789-095-745",
        			"pets": [
        				"Sasha",
        				"Lilly"
        			],
        			"score": 5.8,
        			"email": "trudie5548@hotmail.com",
        			"url": "http://maternity.lib.mi.us",
        			"description": "transcription memorabilia court commodity online oem church restructuring lance material jet elections url autos wooden legislature pen mon controversial studies",
        			"verified": true,
        			"salary": 36784
        		},
        		{
        			"_id": "SQHEJ1UJN9S2OG3B",
        			"name": "Ferne Dodge",
        			"dob": "2014-09-16",
        			"address": {
        				"street": "8428 Squire Road",
        				"town": "Rowley Regis",
        				"postode": "KW1 5DN"
        			},
        			"telephone": "+264-0561-357-213",
        			"pets": [
        				"CoCo",
        				"Cooper"
        			],
        			"score": 1.2,
        			"email": "giamartz@accurate.com",
        			"url": "https://www.hardware.tama.tokyo.jp",
        			"description": "fallen foot moral declaration plans submitting drivers rule samoa duplicate xi wants zone cedar facts contemporary drivers payments height known",
        			"verified": true,
        			"salary": 19567
        		},
        		{
        			"_id": "PEQZ5XCK1N1G8GSM",
        			"name": "Marisol Shipp",
        			"dob": "2020-03-08",
        			"address": {
        				"street": "6879 Huntley",
        				"town": "North Petherton",
        				"postode": "KT7 4ED"
        			},
        			"telephone": "+507-4580-197-201",
        			"pets": [
        				"Izzy",
        				"Charlie"
        			],
        			"score": 10,
        			"email": "edithneumann@jones.com",
        			"url": "http://genome.dental",
        			"description": "sweden jose mastercard presented afternoon targeted horizon stroke recover springs diet glory libs choosing pediatric realtors justice current laos corps",
        			"verified": true,
        			"salary": 12524
        		},
        		{
        			"_id": "8BC65A8VESNQMQTK",
        			"name": "Connie Barr",
        			"dob": "2017-11-08",
        			"address": {
        				"street": "4381 Chestnut Avenue",
        				"town": "Mossley",
        				"postode": "LL78 7BV"
        			},
        			"telephone": "+965-6813-369-079",
        			"pets": [
        				"Daisy",
        				"Apollo"
        			],
        			"score": 2.4,
        			"email": "santos396@equity.com",
        			"url": "https://www.jun.com",
        			"description": "coated disabled jungle addressing hoped airports collector importantly perfectly libraries hereby reseller dressing reasons tracker mountain procedure fusion lease patterns",
        			"verified": true,
        			"salary": 54612
        		},
        		{
        			"_id": "1NQYQR87LIQ77LO5",
        			"name": "Deshawn Rossi",
        			"dob": "2017-10-16",
        			"address": {
        				"street": "1017 Johnson",
        				"town": "Leicester",
        				"postode": "NP6 0CA"
        			},
        			"telephone": "+596-5479-495-356",
        			"pets": [
        				"Fiona",
        				"Henry"
        			],
        			"score": 7,
        			"email": "louriescott@moral.com",
        			"url": "http://www.newspapers.com",
        			"description": "man lg neither item requires experiences read outstanding evolution bunny dialog licensing donation belongs multiple available product photo bb austin",
        			"verified": false,
        			"salary": 55744
        		},
        		{
        			"_id": "8F665QJU855HT5GA",
        			"name": "Elsy Hastings",
        			"dob": "2022-01-12",
        			"address": {
        				"street": "0059 Goodlad Road",
        				"town": "Kidderminster",
        				"postode": "AL8 7MT"
        			},
        			"telephone": "+263-6677-286-946",
        			"pets": [
        				"Harley",
        				"Jack"
        			],
        			"score": 6.7,
        			"email": "winnifred14709@gmail.com",
        			"url": "http://dan.com",
        			"description": "yellow horizontal introduced w these dog forth culture doctor stuff prerequisite retrieval instructors pre publications looking dale auditor estonia accreditation",
        			"verified": false,
        			"salary": 33446
        		},
        		{
        			"_id": "A046T29QUGG7QJUL",
        			"name": "Wes Beauregard",
        			"dob": "2020-10-31",
        			"address": {
        				"street": "9046 Shiredale",
        				"town": "Burry Port",
        				"postode": "DH97 2SD"
        			},
        			"telephone": "+263-2132-162-997",
        			"pets": [
        				"Lucky",
        				"Rosie"
        			],
        			"score": 9.5,
        			"email": "blanca-hastings-oneil@gmail.com",
        			"url": "https://insider.home-webserver.de",
        			"description": "authorized crime between treating fifteen mind physicians enhancement scenes formal booking skirt loop rapid well bidder buildings sao minimize cruise",
        			"verified": true,
        			"salary": 30363
        		},
        		{
        			"_id": "YEPM8BC07Q2KVYRA",
        			"name": "Valene Donald",
        			"dob": "2018-03-26",
        			"address": {
        				"street": "5031 Ashgate Avenue",
        				"town": "Manchester",
        				"postode": "RH85 7EZ"
        			},
        			"telephone": "+43-7106-057-452",
        			"pets": [
        				"Oreo",
        				"Scout"
        			],
        			"score": 2.5,
        			"email": "valencia632@yahoo.com",
        			"url": "http://refugees.from-mi.com",
        			"description": "fig addition program concerning frankfurt premiere the sunrise canberra physician entertaining businesses vol fast attract assured guidance vaccine plots newman",
        			"verified": true,
        			"salary": 41638
        		},
        		{
        			"_id": "PDAQ4XLITD9O5U7M",
        			"name": "Ellamae Ferraro",
        			"dob": "2020-09-07",
        			"address": {
        				"street": "1434 Bridge Street",
        				"town": "Larbert",
        				"postode": "BD2 4LZ"
        			},
        			"telephone": "+352-3112-572-537",
        			"pets": [
        				"Cleo",
        				"Lucky"
        			],
        			"score": 3.6,
        			"email": "junior_stephens@hotmail.com",
        			"url": "https://feelings.com",
        			"description": "defects kingdom hwy liechtenstein january latvia value watershed generator kansas hungry disease hh forbes documentation biggest longer productions belfast manager",
        			"verified": true,
        			"salary": 38826
        		},
        		{
        			"_id": "1JTLBKHUUGXQAMB5",
        			"name": "Ericka Winston",
        			"dob": "2019-06-05",
        			"address": {
        				"street": "2436 Haven",
        				"town": "Hereford",
        				"postode": "OL7 8DP"
        			},
        			"telephone": "+92-4589-055-465",
        			"pets": [
        				"Lily",
        				"Ellie"
        			],
        			"score": 6.6,
        			"email": "mi.bruns1@gmail.com",
        			"url": "http://www.hockey.com",
        			"description": "sans charitable katrina factory pubmed tuesday todd dad carnival translations deeply vice political macintosh hardly affiliate respective issues lowest introducing",
        			"verified": true,
        			"salary": 62691
        		},
        		{
        			"_id": "MCSKZLECUEA2C5PU",
        			"name": "Jennie Wild",
        			"dob": "2021-09-27",
        			"address": {
        				"street": "5855 Moss Lane",
        				"town": "Erith",
        				"postode": "G39 5TB"
        			},
        			"telephone": "+886-2362-259-199",
        			"pets": [
        				"Zoe",
        				"Dexter"
        			],
        			"score": 9.3,
        			"email": "darby-dominquez54107@yahoo.com",
        			"url": "http://trials.com",
        			"description": "henry reprints patrol leasing quad army ins sat latex realm intranet netherlands michel colors great wives restaurants watershed nato america",
        			"verified": true,
        			"salary": 58011
        		},
        		{
        			"_id": "2YQXFPO9CNZ1OIM0",
        			"name": "Luciano Bell",
        			"dob": "2017-01-28",
        			"address": {
        				"street": "5814 Badminton Avenue",
        				"town": "Driffield",
        				"postode": "GU39 2GZ"
        			},
        			"telephone": "+237-0769-305-664",
        			"pets": [
        				"Oscar",
        				"Marley"
        			],
        			"score": 9.2,
        			"email": "raleigh.santoro@gmail.com",
        			"url": "https://www.laden.com",
        			"description": "charging inch annotated strings unwrap commission bank protected luxury label reef reserve belize devoted forecast gaming similar tool assistance gays",
        			"verified": true,
        			"salary": 37970
        		},
        		{
        			"_id": "26F8PT8LYUB2I1JX",
        			"name": "Harland Burrow",
        			"dob": "2022-11-11",
        			"address": {
        				"street": "2406 Levedale Circle",
        				"town": "Jarrow",
        				"postode": "CH94 2IY"
        			},
        			"telephone": "+264-9589-530-892",
        			"pets": [
        				"Boots",
        				"Duke"
        			],
        			"score": 9.2,
        			"email": "saundra.dickinson45808@hotmail.com",
        			"url": "http://conservative.com",
        			"description": "nz athletes failing greece her thomson keeping quantities fc pet ships biblical sm necessary nursery spell injection way solution governmental",
        			"verified": true,
        			"salary": 53227
        		},
        		{
        			"_id": "M4MPZ8QTILYZEPVG",
        			"name": "Micha Moon",
        			"dob": "2015-04-26",
        			"address": {
        				"street": "2625 Brecon",
        				"town": "Aviemore",
        				"postode": "WS2 5WZ"
        			},
        			"telephone": "+968-3199-782-340",
        			"pets": [
        				"Fiona",
        				"Riley"
        			],
        			"score": 7.4,
        			"email": "minta94202@sheriff.tc",
        			"url": "https://hobbies.com",
        			"description": "fibre columbia di informed reef indication lip component theatre philadelphia las attention comm received composition nov pirates mercy houston pine",
        			"verified": false,
        			"salary": 45216
        		},
        		{
        			"_id": "YXSG4RLXKEQ4EQ23",
        			"name": "Misha Hargis-Badger",
        			"dob": "2015-10-07",
        			"address": {
        				"street": "9782 Bonville Circle",
        				"town": "Cockermouth",
        				"postode": "SL5 7DG"
        			},
        			"telephone": "+60-0104-691-642",
        			"pets": [
        				"Sammy",
        				"Duke"
        			],
        			"score": 6.1,
        			"email": "gussiegulley36@photographer.com",
        			"url": "https://supplier.com",
        			"description": "arrives subscriber retained obvious crystal sol remote logitech angel toe consistent pants membership popular sql disappointed survey msn incorporated beautiful",
        			"verified": true,
        			"salary": 30320
        		},
        		{
        			"_id": "CF1PCYPO65ABC8S1",
        			"name": "Usha Hutton",
        			"dob": "2017-05-05",
        			"address": {
        				"street": "8244 Thornden Circle",
        				"town": "Maldon",
        				"postode": "HD20 3VS"
        			},
        			"telephone": "+49-4694-546-795",
        			"pets": [
        				"bailey",
        				"Shadow"
        			],
        			"score": 2.2,
        			"email": "marita-kauffman@upgrades.com",
        			"url": "http://www.charge.com",
        			"description": "medium harper climbing suse patients kelly doc cj eating telecom lost soil restoration wellington vault muscles determined promotional managing painful",
        			"verified": false,
        			"salary": 32091
        		},
        		{
        			"_id": "KHSQBLDRJS29FFNQ",
        			"name": "Sharen Mahon",
        			"dob": "2019-06-22",
        			"address": {
        				"street": "3572 Roosevelt Circle",
        				"town": "Southsea",
        				"postode": "MK40 8CC"
        			},
        			"telephone": "+509-0783-559-952",
        			"pets": [
        				"MIMI",
        				"Apollo"
        			],
        			"score": 2.9,
        			"email": "cassaundra.ogden@experiments.com",
        			"url": "https://exciting.com",
        			"description": "avon bookmark guyana correction colored concerts medline fence things fun comments aims behavior concerts wood particles structures drawn late eur",
        			"verified": true,
        			"salary": 40932
        		},
        		{
        			"_id": "3BFQ98IC0N611M8Y",
        			"name": "Myrna Block",
        			"dob": "2015-10-10",
        			"address": {
        				"street": "9756 Downton Avenue",
        				"town": "Tranent",
        				"postode": "CH8 3PK"
        			},
        			"telephone": "+27-2834-868-866",
        			"pets": [
        				"Kitty",
        				"Marley"
        			],
        			"score": 8.3,
        			"email": "whitleyspangler@hotmail.com",
        			"url": "https://www.saturn.com",
        			"description": "few restructuring vsnet yea forgot gmt afterwards township digest nn might acrobat engineering co gnu indoor backup oem corner accomplish",
        			"verified": true,
        			"salary": 37936
        		},
        		{
        			"_id": "0AH1BEXYO0NKYRCM",
        			"name": "Brant Ogden",
        			"dob": "2021-09-01",
        			"address": {
        				"street": "9897 Midhurst Lane",
        				"town": "Portishead",
        				"postode": "BL8 5UO"
        			},
        			"telephone": "+598-7536-682-209",
        			"pets": [
        				"Cali",
        				"Buddy"
        			],
        			"score": 3,
        			"email": "coralie_heilman@lands.com",
        			"url": "http://ping.com",
        			"description": "dakota kings antigua method approaches occupied filtering agreed elements generic publicly poverty lewis model ftp pakistan willow guards reel acts",
        			"verified": false,
        			"salary": 60547
        		},
        		{
        			"_id": "SMSNN0UK8EVID3OQ",
        			"name": "Alvaro Estes",
        			"dob": "2016-08-03",
        			"address": {
        				"street": "5187 Ashbourne Street",
        				"town": "New Quay",
        				"postode": "TA23 8IA"
        			},
        			"telephone": "+968-6146-170-706",
        			"pets": [
        				"Frankie",
        				"Henry"
        			],
        			"score": 8.5,
        			"email": "gale-seifert50@corruption.com",
        			"url": "https://emotions.com",
        			"description": "loan displaying cause mon mainland seo characteristic agencies protection glen dedicated representative headline records disclaimers width lookup basketball liabilities wagon",
        			"verified": true,
        			"salary": 46244
        		},
        		{
        			"_id": "CEHLEBI1T64OSPPK",
        			"name": "Shawna Thiel",
        			"dob": "2017-02-24",
        			"address": {
        				"street": "8573 Gillingham Circle",
        				"town": "Stokesley",
        				"postode": "TW3 6YB"
        			},
        			"telephone": "+241-0681-315-106",
        			"pets": [
        				"Buddy",
        				"Duke"
        			],
        			"score": 9.8,
        			"email": "glenniespencer41@hotmail.com",
        			"url": "http://www.message.com",
        			"description": "certification affect opposite wisconsin assembled ns inside magnificent received dave body roots assistant precision wire speak ram des diy studio",
        			"verified": true,
        			"salary": 49137
        		},
        		{
        			"_id": "ATTAEOSDEMJCDLDJ",
        			"name": "Kirk Cleveland",
        			"dob": "2019-11-29",
        			"address": {
        				"street": "6339 Artists",
        				"town": "Hockley",
        				"postode": "TN4 1KC"
        			},
        			"telephone": "+687-1298-392-561",
        			"pets": [
        				"Kitty",
        				"Cody"
        			],
        			"score": 3.7,
        			"email": "dudleystuckey4538@ca.com",
        			"url": "http://www.ago.com",
        			"description": "rochester annex prototype lc giants purchasing beatles rpm submissions doctrine socks bachelor runtime passive s fifth ferrari fishing automated china",
        			"verified": true,
        			"salary": 60860
        		},
        		{
        			"_id": "QBN0GIBF2Y62L793",
        			"name": "Dorla Chapman",
        			"dob": "2018-07-23",
        			"address": {
        				"street": "9817 Mattison",
        				"town": "Farnborough",
        				"postode": "SM74 5EV"
        			},
        			"telephone": "+971-8528-476-539",
        			"pets": [
        				"Rusty",
        				"Max"
        			],
        			"score": 3,
        			"email": "georgenecarrasco-mcnamee87@emerald.com",
        			"url": "https://precisely.com",
        			"description": "peaceful bargains archive suffered baths steady pop communicate entertainment glenn successfully system levy tire keeps developments joins battlefield rope weeks",
        			"verified": true,
        			"salary": 49009
        		},
        		{
        			"_id": "APUP4O5V6Y0L0FOM",
        			"name": "Lorretta Hutchins",
        			"dob": "2021-07-07",
        			"address": {
        				"street": "0581 Polefield Avenue",
        				"town": "Nantwich",
        				"postode": "KA22 1AT"
        			},
        			"telephone": "+592-4970-580-423",
        			"pets": [
        				"Phoebe",
        				"Duke"
        			],
        			"score": 8.8,
        			"email": "odeliadavenport@hotmail.com",
        			"url": "https://extending.com",
        			"description": "requests tracy exchange inquiries duo importance collector including modify norway rack insulin boots physician threatened bali permits sussex fonts presents",
        			"verified": true,
        			"salary": 27235
        		},
        		{
        			"_id": "A0KAFOLKOC32UD31",
        			"name": "Jo Shields",
        			"dob": "2014-06-28",
        			"address": {
        				"street": "0060 Hus",
        				"town": "Insch",
        				"postode": "SR09 6FM"
        			},
        			"telephone": "+91-0751-361-536",
        			"pets": [
        				"Rocky",
        				"Sasha"
        			],
        			"score": 4,
        			"email": "syreeta5@flour.com",
        			"url": "http://www.daily.com",
        			"description": "westminster media prints belfast distinction theater improved customize via immediate sympathy post notices enormous decision forget factor entertaining tool simultaneously",
        			"verified": true,
        			"salary": 57058
        		},
        		{
        			"_id": "01BX108KTUF9QQ7E",
        			"name": "Branden Galloway",
        			"dob": "2018-09-24",
        			"address": {
        				"street": "6815 Wallingford Road",
        				"town": "Burntisland",
        				"postode": "BT2 5BD"
        			},
        			"telephone": "+852-6396-906-564",
        			"pets": [
        				"Gracie",
        				"Duke"
        			],
        			"score": 8.6,
        			"email": "lala_kessler73636@encourages.com",
        			"url": "http://arabic.com",
        			"description": "elevation id specialized smoking pmid fwd persons quantities console cancer boxing baby chem rule hong reviewer horse comfortable olive dh",
        			"verified": false,
        			"salary": 21792
        		}
        	]
        }
    """.trimIndent()

    private val simpleJson = """
    {
    	"object": {
            "longString": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
    	}
    }
""".trimIndent()

    private val emptyJson = "{}"

}