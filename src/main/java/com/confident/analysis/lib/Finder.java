package com.confident.analysis.lib;

import com.confident.analysis.Loader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * Find any string from particular source
 *
 * @since 20160327
 * @author Catur Adi Nugroho
 *
 */
public class Finder {

    // 0. Specify the analyzer for tokenizing text.
    //    The same analyzer should be used for indexing and searching
    private static StandardAnalyzer analyzer = new StandardAnalyzer();

    // 1. create the index
    private static Directory index = new RAMDirectory();

    private static final String NAME = "name";
    private static final String NICKNAME = "nickName";

    //(^|\s+)(CO|THE|LC|LLC|LTD|LAND|LOAN|L L C|INC|TRAVEL|E?STATES?|DIVISION|LIMITED|COMPANY|PARTNERSHIP)($|\s+)
    private static final Pattern COMPANY_SUFFIX = 
            Pattern.compile("(^|\\s+)(CO|THE|LC|LLC|LTD|LAND|LOAN|L L C|INC|TRAVEL|E?STATES?|DIVISION|LIMITED|COMPANY|PARTNERSHIP)($|\\s+)");

    //(^|\s+)(COMPANY\s)?(CO|THE|LC|LLC|LTD|L L C|INC|LIMITED|COMPANY|PARTNERSHIP)?(\s?(ET AL|PARTNERSHIP))?($|\s+)
    private static final Pattern ESCAPE_COMPANY_SUFFIX = 
            Pattern.compile("(^|\\s+)(COMPANY\\s)?(CO|THE|LC|LLC|LTD|L L C|INC|LIMITED|COMPANY|PARTNERSHIP)?(\\s?(ET AL|PARTNERSHIP))?($|\\s+)");
    private static final Pattern WORD = Pattern.compile("[^A-Z\\-\\s](\\s&\\s)?");
    //\s*-+\s*|\s*&\s*$|\s{2,}
    private static final Pattern SPACE = Pattern.compile("\\s*-+\\s*|\\s*&\\s*$|\\s{2,}");
    
    //[^0-9A-Z\\s]
    private static final Pattern WORD_NUMBER = Pattern.compile("[^0-9A-Z\\s]");

    /**
     *
     * @param string
     * @return
     */
    public static boolean isCompanyName(String string) {
        Matcher m = COMPANY_SUFFIX.matcher(string);
        boolean result = m.find();
        if (!result) {
            result = string.indexOf(',', 2) == -1;
        }
        return result;
    }

    /**
     *
     * @param sName
     * @return
     */
    public String duplicateName(String sName) {
        String s = cleanWord(sName);
        s = clearCompanySuffix(s);
        s = cleanSpace(s).trim();
        if (s.length() < 3) {
            s = cleanWordNumber(sName);
        }
        s = s + Loader.NAME_DELIMITER;
        return s.concat(sName);
    }
    
    private String cleanWord(String s) {
        return WORD.matcher(s).replaceAll("");
    }
    
    private String cleanWordNumber(String s) {
        return WORD_NUMBER.matcher(s).replaceAll("");
    }
    
    private String clearCompanySuffix(String s) {
        return ESCAPE_COMPANY_SUFFIX.matcher(s).replaceAll("");
    }
    
    private String cleanSpace(String s) {
        return SPACE.matcher(s).replaceAll(" ");
    }

    /**
     *
     * @throws IOException
     * @throws ParseException
     */
    public static void init() throws IOException, ParseException {

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w = new IndexWriter(index, config);
        addDoc(w, "Abel", "Ab, Abe, Eb, Ebbie");
        addDoc(w, "Abiah/Abijah", "A.B., Ab, Biah");
        addDoc(w, "Abiel", "Biel, Ab");
        addDoc(w, "Abigail", "Abby, Abbie, Nabby, Gail");
        addDoc(w, "Abner", "Ab");
        addDoc(w, "Abraham/Abram", "Abe");
        addDoc(w, "Adaline/Adeline", "Ada, Addy, Dell, Delia, Lena");
        addDoc(w, "Adam", "Ad, Ade");
        addDoc(w, "Adelaide", "Addy, Adele, Dell, Della, Heidi");
        addDoc(w, "Adelbert", "Ad, Ade, Albert, Bert, Del, Delbert, Elbert");
        addDoc(w, "Adelphia", "Adele, Addy, Dell, Delphia, Philly");
        addDoc(w, "Adolph/Adolphus", "Ad, Dolph, Olph");
        addDoc(w, "Agatha", "Aggy");
        addDoc(w, "Agnes", "Aggy, Inez, Nessa");
        addDoc(w, "Aileen", "Allie, Lena");
        addDoc(w, "Alabama", "Bama, Al");
        addDoc(w, "Alabama", "Bama");
        addDoc(w, "Alan", "Al");
        addDoc(w, "Alanson", "Alan, Al, Lonson");
        addDoc(w, "Albert", "Al, Bert, Elbert, see also Adelbert");
        addDoc(w, "Alberta", "Allie, Bert, Bertie");
        addDoc(w, "Aldrich", "Al, Rich, Richie");
        addDoc(w, "Alexander", "Al, Alex, Eleck, Sandy");
        addDoc(w, "Alexandra", "Alex, Alla, Sandy");
        addDoc(w, "Alfred", "Al, Fred");
        addDoc(w, "Alfreda", "Alfy, Freda, Freddy, Frieda");
        addDoc(w, "Alice/Alicia", "Allie, Elsie, Lisa");
        addDoc(w, "Allison", "Al, Ali, Ally");
        addDoc(w, "Almeda", "Mary");
        addDoc(w, "Almena", "Allie, Mena");
        addDoc(w, "Alonzo", "Al, Lon, Lonas, Lonzo");
        addDoc(w, "Amanda", "Manda, Mandy");
        addDoc(w, "Amelia", "Emily, Mel, Millie, Amy");
        addDoc(w, "Anderson", "Ander, Andy, Sonny");
        addDoc(w, "Andrew", "Andy, Drew");
        addDoc(w, "Ann/Anne", "Agnes, Annie, Nan, Nanny, Nana, Nancy (and");
        addDoc(w, "Annaka", "Ann, Anne, Annie, Nancy, Niki");
        addDoc(w, "Anthony", "Tony, Tunis");
        addDoc(w, "Antoinette/Antonia", "Ann, Tony, Netta");
        addDoc(w, "Arabella/Arabelle", "Ara, Arry, Belle, Bella");
        addDoc(w, "Archibald", "Archie, Baldo");
        addDoc(w, "Arlene", "Arly, Lena");
        addDoc(w, "Armena", "Arry, Mena");
        addDoc(w, "Arthur", "Art");
        addDoc(w, "Asahel", "Asa");
        addDoc(w, "Asaph", "Asa");
        addDoc(w, "Asenath", "Assene, Natty, Sene");
        addDoc(w, "Ashley", "Ash");
        addDoc(w, "Ashley", "Ash");
        addDoc(w, "Asmine", "Amy");
        addDoc(w, "Augusta/Augustina", "Aggy, Gatsy, Gussie, Tina");
        addDoc(w, "Augustus/Augustine", "August, Austin, Gus");
        addDoc(w, "Azariah", "Aze, Riah");
        addDoc(w, "Azubah", "Zubia");
        addDoc(w, "Barbara", "Bab, Babs, Barby, Bobbie");
        addDoc(w, "Barnabas", "Barney");
        addDoc(w, "Bartholomew", "Bart, Bartel, Bat, Mees, Meus");
        addDoc(w, "Basil", "Baz");
        addDoc(w, "Beatrice", "Bea, Trisha, Trix, Trixie");
        addDoc(w, "Belinda", "Belle, Linda");
        addDoc(w, "Benedict", "Ben, Bennie");
        addDoc(w, "Benjamin Franklin", "Often found as B.F.");
        addDoc(w, "Benjamin", "Ben, Bennie, Benjy, Jamie");
        addDoc(w, "Bernadette", "Bunny");
        addDoc(w, "Bernard", "Barney, Berney");
        addDoc(w, "Bertha", "Birdie, Bert, Bertie");
        addDoc(w, "Birt", "Bird");
        addDoc(w, "Bradford", "Brad, Ford");
        addDoc(w, "Bridget", "Biddie, Biddy, Bridgie, Bridie");
        addDoc(w, "Broderick", "Ricky, Brady, Brody, Rod");
        addDoc(w, "Caleb", "Cal, Cale, Calep");
        addDoc(w, "Calvin", "Cal, Vin, Vinny");
        addDoc(w, "Cameron", "Cam, Ronny, Ron");
        addDoc(w, "Camille", "Cammie, Millie");
        addDoc(w, "Carl", "Charles, Charlie, Charley");
        addDoc(w, "Carol/Caroline/Carolyn", "Carrie, Cassie, Lynn");
        addDoc(w, "Casey, Kasey", "K.C.");
        addDoc(w, "Cassandra", "Cassie, Sandra, Sandy");
        addDoc(w, "Catherine/Cathleen/Katherine/Kathleen", "Cathy, Kathy, Katy, Cassie, Cat, Kay, Kit, Kittie, Trina, Lena");
        addDoc(w, "Catriona", "Catherine");
        addDoc(w, "Cecilia", "Celia, Cissy, Sissie");
        addDoc(w, "Cedric", "Ced, Rick, Ricky");
        addDoc(w, "Charles", "Carl, Charlie, Chick, Chuck");
        addDoc(w, "Charlotte", "Char, Lottie, Lotta, Sherry");
        addDoc(w, "Chauncey", "Chan");
        addDoc(w, "Chester", "Chet");
        addDoc(w, "Christine/Christina/Christiana/Kristine, etc.", "Chris, Kris, Crissy, Christy, Kristy, Tina");
        addDoc(w, "Christopher/Christian", "Chris, Kit, Topher");
        addDoc(w, "Cinderella", "Cindy");
        addDoc(w, "Clarence", "Clair, Clare");
        addDoc(w, "Clarinda", "Clara");
        addDoc(w, "Clarissa", "Clara, Cissy");
        addDoc(w, "Claudia", "Claud, Caya");
        addDoc(w, "Clement", "Clem");
        addDoc(w, "Clifford", "Cliff, Ford");
        addDoc(w, "Clifton", "Cliff, Tony");
        addDoc(w, "Columbus", "Clum");
        addDoc(w, "Conrad", "Con, Conny, Connie");
        addDoc(w, "Constance", "Connie");
        addDoc(w, "Cordelia", "Cordy, Delia");
        addDoc(w, "Cornelia", "Cornie, Nelia");
        addDoc(w, "Cornelia", "Corny, Nelle, Nelly, Nelia");
        addDoc(w, "Cornelius", "Con, Conny, Corny, Niel");
        addDoc(w, "Courtney", "Court, Curt");
        addDoc(w, "Curtis", "Curt");
        addDoc(w, "Cynthia", "Cindy, Cintha");
        addDoc(w, "Cyrenius", "Cene, Cy, Renius, Serene, Swene");
        addDoc(w, "Cyrus", "Cy, Si");
        addDoc(w, "Dalton", "Dal");
        addDoc(w, "Daniel", "Dan, Danny");
        addDoc(w, "Darlene", "Lena, Dara, Darry");
        addDoc(w, "David", "Dave, Davey, Day");
        addDoc(w, "Dawson", "Dawse, Doss, Dos");
        addDoc(w, "Deborah/Debora", "Debby, Debbie, Deb");
        addDoc(w, "Delbert", "Bert, Del");
        addDoc(w, "Deliverance", "Della, Delly, Dilly");
        addDoc(w, "Delores", "Dell, Lola, Lolly, Della, Dee");
        addDoc(w, "Dennis", "Dennie, Denny");
        addDoc(w, "Dennison", "Dennis, Dennie, Denny");
        addDoc(w, "Derrick", "Eric, Rick, Ricky");
        addDoc(w, "Dianne", "Di, Ann");
        addDoc(w, "Dicey", "Dicie");
        addDoc(w, "Donald", "Don, Donny");
        addDoc(w, "Dorcus", "Darkey, Darcus");
        addDoc(w, "Dorothy", "Dolly, Dot, Dortha, Dotty");
        addDoc(w, "Dorothy", "Dora, Dot, Dottie");
        addDoc(w, "Ebenezer", "Eben, Eb, Ebbie");
        addDoc(w, "Edith", "Edie");
        addDoc(w, "Edmund", "Ed, Ned, Ted, Eddie");
        addDoc(w, "Edward", "Ed, Ned, Ted, Teddy, Eddie");
        addDoc(w, "Edwin", "Ed, , Eddie, Ned, Win");
        addDoc(w, "Elbert", "Albert, Bert, Birt, Bird");
        addDoc(w, "Eldora", "Dora");
        addDoc(w, "Eleanor", "Elaine, Ellen, Ellie, Lanna, Lenora, Nelly, Nora");
        addDoc(w, "Eleazer", "Lazar");
        addDoc(w, "Elias", "Eli, Lee, Lias");
        addDoc(w, "Elijah", "Eli, Lige");
        addDoc(w, "Eliphalet", "Left");
        addDoc(w, "Elise", "Ish");
        addDoc(w, "Elisha", "Eli, Lish");
        addDoc(w, "Elizabeth", "Eliza, Bess, Bessie, Beth, Betsy, Betty, Lib, Libby, Liza, Lisa, Liz, Lizzie");
        addDoc(w, "Ellen", "see Eleanor and Helen");
        addDoc(w, "Ellswood/Elswood", "Elsey/Elze/Elsie");
        addDoc(w, "Elmira", "Elly, Ellie, Mira");
        addDoc(w, "Elouise", "Louise");
        addDoc(w, "Elvira", "Evie");
        addDoc(w, "Elwood", "Woody");
        addDoc(w, "Emanuel", "Manny, Manuel");
        addDoc(w, "Emeline", "Emma, Emily, Emmy, Millie, Em");
        addDoc(w, "Emily", "Emmy, Millie, Emma, Em");
        addDoc(w, "Enrique", "Quique");
        addDoc(w, "Epaphroditius", "Dite, Ditus, Dyce, Dyche, Eppa");
        addDoc(w, "Ephraim", "Eph");
        addDoc(w, "Eric", "Rick, Ricky, Derrick");
        addDoc(w, "Ernest", "Ernie");
        addDoc(w, "Estelle/Estella", "Essy, Essie,  Stella");
        addDoc(w, "Esther", "Essie, Essy");
        addDoc(w, "Eugene", "Gene");
        addDoc(w, "Evaline", "Eva, Eve, Lena");
        addDoc(w, "Ezekiel", "Ez, Zeke");
        addDoc(w, "Ezra", "Ez, Ezry, Ezrie");
        addDoc(w, "Faith", "Fay");
        addDoc(w, "Ferdinand", "Ferdie");
        addDoc(w, "Fidelia", "Delia");
        addDoc(w, "Florence", "Flo, Flora, Flossy");
        addDoc(w, "Frances", "Fanny, Fran, Cissy, Frankie, Sis");
        addDoc(w, "Francis", "Frank, Fran");
        addDoc(w, "Franklin", "Frank, Fran");
        addDoc(w, "Frederick", "Fred, Freddy, Fritz");
        addDoc(w, "Fredericka", "Freda, Freddy, Ricka, Frieda");
        addDoc(w, "Gabriel", "Gabe, Gabby");
        addDoc(w, "Gabrielle/Gabriella", "Gabby, Ella");
        addDoc(w, "Genevieve", "Eve, Jean, Jenny");
        addDoc(w, "Geoffrey/Jeffrey", "Geoff, Jeff");
        addDoc(w, "George Washington", "Often seen as G.W. or Wash");
        addDoc(w, "Gerald", "Jerry, Gerry");
        addDoc(w, "Geraldine", "Dina, Gerry, Gerrie, Jerry");
        addDoc(w, "Gerardo", "Gera");
        addDoc(w, "Gertrude", "Trudy, Gert, Gertie");
        addDoc(w, "Gilbert", "Gil, Bert, Wilber");
        addDoc(w, "Grizzel", "Grace");
        addDoc(w, "Gwendolyn", "Gwen, Wendy");
        addDoc(w, "Hannah", "Nan, Nanny, Anna");
        addDoc(w, "Harold", "Hal, Harry");
        addDoc(w, "Harriet", "Hattie");
        addDoc(w, "Helen/Helene", "Ella, Ellen, Ellie, Lena");
        addDoc(w, "Heloise", "Eloise, Lois");
        addDoc(w, "Henrietta", "Etta, Etty, Hank, Henny, Henri, Nettie, Retta, Yetta");
        addDoc(w, "Henry", "Hal, Hank, Harry");
        addDoc(w, "Hepsibah/Hephsibah", "Hipsie");
        addDoc(w, "Herbert", "Herb, Bert");
        addDoc(w, "Hermione", "Hermie");
        addDoc(w, "Hester", "Hessy, Esther, Hetty");
        addDoc(w, "Hezekiah", "Hez, Hy, Kiah");
        addDoc(w, "Hiram", "Hy");
        addDoc(w, "Hopkins", "Hop, Hopp");
        addDoc(w, "Horace", "Horry");
        addDoc(w, "Hubert", "Hugh, Bert, Hub");
        addDoc(w, "Ian", "John");
        addDoc(w, "Ignacio", "Nacho");
        addDoc(w, "Ignatius", "Iggy, Nace, Nate, Natius");
        addDoc(w, "Irene", "Rena");
        addDoc(w, "Isaac", "Ike, Zeke");
        addDoc(w, "Isabel/Isabelle/Isabella", "Bella, Belle, Eba, Ebba, Elba, Ib, Ibbee, Issy, Nib, Nibby, Tibbie");
        addDoc(w, "Isadora", "Dora, Issy");
        addDoc(w, "Isidore", "Izzy");
        addDoc(w, "Israel", "Ziggy");
        addDoc(w, "Jacob", "Jaap, Jake, Jay, could be short for Jacobus.");
        addDoc(w, "James", "Jamie, Jem, Jim, Jimmy");
        addDoc(w, "Jane", "Janie, Jean, Jennie, Jessie");
        addDoc(w, "Janet", "Jessie, Jan, Jane");
        addDoc(w, "Jean/Jeanne", "Jane, Jeannie");
        addDoc(w, "Jeanette", "Janet, Jean, Jessie, Nettie");
        addDoc(w, "Jebadiah", "Jeb");
        addDoc(w, "Jedidiah", "Jed");
        addDoc(w, "Jefferson", "Jeff, Sonny");
        addDoc(w, "Jehiel", "Hiel");
        addDoc(w, "Jemima", "Mima");
        addDoc(w, "Jennifer", "Jennie");
        addDoc(w, "Jeptha", "Jep, Nep");
        addDoc(w, "Jeremiah", "Jereme, Jerry");
        addDoc(w, "Jesse", "Jess, Jessie");
        addDoc(w, "Jessica", "Jessie, Jess");
        addDoc(w, "Jessie", "Jess, Jesse");
        addDoc(w, "Joan", "Nonie, Nona");
        addDoc(w, "Joanna/Johannah", "Joan, Jody, Hannah, Jo");
        addDoc(w, "Johann", "John");
        addDoc(w, "John", "Jack, Jock, Johnny");
        addDoc(w, "Jonathan", "Jon, John, Nathan");
        addDoc(w, "Jose", "Pepe");
        addDoc(w, "Joseph", "Joe, Joey, Jos, Jody");
        addDoc(w, "Josephine", "Jody, Jo, Joey, Josey, Fina");
        addDoc(w, "Joshua", "Josh, Joe");
        addDoc(w, "Josiah", "Joe, Si");
        addDoc(w, "Joyce", "Joy");
        addDoc(w, "Juanita", "Nettie, Nita");
        addDoc(w, "Judith ", " Judy");
        addDoc(w, "Judson", "Jud, Sonny");
        addDoc(w, "Julia", "Julie, Jill");
        addDoc(w, "Julias/Julian", "Jule, Jules");
        addDoc(w, "Junior", "JR, June, Junie");
        addDoc(w, "Kasey/Casy", "K.C.");
        addDoc(w, "Kassandra", "Kassie");
        addDoc(w, "Katherine", "see Catherine");
        addDoc(w, "Kenneth", "Ken, Kenny");
        addDoc(w, "Keziah", "Kizza, Kizzie");
        addDoc(w, "Kingsley", "King");
        addDoc(w, "Kingston", "King");
        addDoc(w, "Lafayette", "Fate, Laffie");
        addDoc(w, "Lamont", "Monty");
        addDoc(w, "Lavina/Lavinia", "Ina, Vina, Viney");
        addDoc(w, "Lawrence/Laurence", "Larry, Lars, Lon, Lonny, Lorne, Lorry,");
        addDoc(w, "LeRoy", "L.R., Lee, Roy");
        addDoc(w, "Lemuel", "Lem");
        addDoc(w, "Lenora", "Nora, Lee, see Eleanor");
        addDoc(w, "Leonard", "Leo, Leon, Len, Lenny, Lineau");
        addDoc(w, "Leslie", "Les");
        addDoc(w, "Leslie", "Lizzy, Les");
        addDoc(w, "Lester", "Les");
        addDoc(w, "Letitia", "Lettie, Lettice, Titia, Tish, Tisha");
        addDoc(w, "Levi", "Lee");
        addDoc(w, "Lillah", "Lily, Lilly, Lil, Lolly");
        addDoc(w, "Lillian", "Lil, Lilly, Lolly");
        addDoc(w, "Lincoln", "Link");
        addDoc(w, "Linda", "Lindy, Lynn, Melinda, Philinda");
        addDoc(w, "Lorenzo", "Loren");
        addDoc(w, "Loretta", "Etta, Lorrie, Retta, Lottie");
        addDoc(w, "Lorraine", "Lorrie");
        addDoc(w, "Louise/Louisa", "Lou, Eliza, Lois");
        addDoc(w, "Lucias/Lucas", "Luke");
        addDoc(w, "Lucille", "Lu, Lou, Cille, Lucy");
        addDoc(w, "Lucinda", "Cindy, Lu, Lou, Lucy");
        addDoc(w, "Lucretia", "Creasy, Lucy");
        addDoc(w, "Luella", "Ella, Lu, Lula");
        addDoc(w, "Luis", "Luie");
        addDoc(w, "Luther", "Luke");
        addDoc(w, "Lydia/Lidia", "Lyddy");
        addDoc(w, "Lyndon", "Lynn, Lindy");
        addDoc(w, "Madeline", "Lena, Maddy, Madge, Magda, Maggie, Maida, Maud");
        addDoc(w, "Magdelina", "Lena, Madge, Magda, Maggie");
        addDoc(w, "Mahala", "Hallie, Mahaley");
        addDoc(w, "Malachi", "Mal");
        addDoc(w, "Malcolm", "Mal");
        addDoc(w, "Marcus", "Mark");
        addDoc(w, "Margaret/Margaretta", "Daisy, Gretta, Madge, Maggie, Margery, Marge, Margie, Margo, Meg, Midge, Peg, Peggy, Rita,");
        addDoc(w, "Martha", "Marty, Mat, Mattie, Patsy, Patty");
        addDoc(w, "Martin", "Marty");
        addDoc(w, "Marvin", "Marv");
        addDoc(w, "Mary", "Molly, Polly, Mae, Mamie, Mitzi, Sukey");
        addDoc(w, "Matilda", "Tilly, Matty, Maud");
        addDoc(w, "Matthew/Matthias", "Matt, Thias, Thys");
        addDoc(w, "Maurice/Morris", "Morey");
        addDoc(w, "Mehitabel", "Hetty, Hitty, Mabel, Mitty");
        addDoc(w, "Melinda", "Linda, Lindy, Mel, Mindy");
        addDoc(w, "Melissa", "Lisa, Lissa, Lyssa, Mel, Milly, Missy, Wessa");
        addDoc(w, "Melvina", "Mellie, Melly, Mel");
        addDoc(w, "Mervin", "Merv");
        addDoc(w, "Michael", "Ike, Mike, Micah, Mick, Mickey");
        addDoc(w, "Michelle", "Chelle, Mica, Micha, Miche, Mickey, Shelly");
        addDoc(w, "Mildred", "Milly");
        addDoc(w, "Millicent", "Milly, Missy");
        addDoc(w, "Minerva", "Minnie");
        addDoc(w, "Miranda", "Mandy, Mira, Randy");
        addDoc(w, "Miriam", "Mimi, Mitzi");
        addDoc(w, "Mitchell", "Mickey, Mitch");
        addDoc(w, "Monique", "Monike, Mon");
        addDoc(w, "Montgomery", "Monty, Gum");
        addDoc(w, "Morag", "Merran, Marion, Sarah, Sally");
        addDoc(w, "Nancy", "Nan, Nannie");
        addDoc(w, "Napoleon", "Nap, Nappy, Leon");
        addDoc(w, "Natalie", "Natty, Nettie, Tally");
        addDoc(w, "Nathan", "Nate");
        addDoc(w, "Nathaniel", "Nathan, Nate, Nat, Natty, Than");
        addDoc(w, "Newton", "Newt");
        addDoc(w, "Nicholas", "Nick, Nickie, Claas, Claes");
        addDoc(w, "Norbert", "Bert, Norby");
        addDoc(w, "Obediah", "Diah, Dyer, Obed, Obie");
        addDoc(w, "Olive/Olivia", "Ollie, Nollie, Livia, Libby");
        addDoc(w, "Oliver", "Ollie, Obbie, Obby");
        addDoc(w, "Orange", "Ora");
        addDoc(w, "Oswald", "Ossy, Ozzy, Waldo");
        addDoc(w, "Parmelia", "Amelia, Melia, Milly");
        addDoc(w, "Parthena", "Thena");
        addDoc(w, "Patience", "Pat, Patty");
        addDoc(w, "Patricia", "Pat, Patty, Patsy, Tricia");
        addDoc(w, "Patrick", "Paddy, Pat, Patsy, Pate");
        addDoc(w, "Paula/Paulina", "Polly, Lina");
        addDoc(w, "Pelegrine", "Perry");
        addDoc(w, "Penelope", "Penny");
        addDoc(w, "Percival", "Percy");
        addDoc(w, "Peter", "Pete, Pate, Perry");
        addDoc(w, "Philetus", "Leet, Phil");
        addDoc(w, "Philinda", "Linda");
        addDoc(w, "Phillip", "Phil");
        addDoc(w, "Piper", "Pi, Pie");
        addDoc(w, "Prescott", "Scott, Scotty, Pres");
        addDoc(w, "Priscilla", "Cissy, Cilla, Prissy");
        addDoc(w, "Prudence", "Prudy, Prue, Puss");
        addDoc(w, "Rachel", "Shelly");
        addDoc(w, "Randal", "Randy");
        addDoc(w, "Randle", "Randy");
        addDoc(w, "Randolph", "Randy, Dolph");
        addDoc(w, "Raymond", "Ray");
        addDoc(w, "Rebecca", "Reba, Becca, Becky, Beck");
        addDoc(w, "Regina", "Reggie, Gina");
        addDoc(w, "Reginald", "Reg, Reggie, Naldo, Renny");
        addDoc(w, "Relief", "Leafa");
        addDoc(w, "Reuben", "Rube");
        addDoc(w, "Richard", "Dick, Dickon, Rich, Rick, Ricky");
        addDoc(w, "Robert", "Bob, Dob, Dobbin, Hob, Hobkin, Rob, Robby, Bobby, Robin, Rupert");
        addDoc(w, "Roberta", "Bobbie, Robbie, Bert, Bertie or Birdie");
        addDoc(w, "Roger/Rodger", "Roge, Hodge, Rod");
        addDoc(w, "Roland", "Lanny, Rollo, Rolly");
        addDoc(w, "Ronald", "Ron, Ronnie, Naldo");
        addDoc(w, "Rosabel/Rosabella", "Belle, Rosa, Rose, Roz");
        addDoc(w, "Rosalyn/Rosalinda", "Rosa, Rose, Linda, Roz");
        addDoc(w, "Roseann/Roseanna", "Rose, Ann, Roz, Rosie");
        addDoc(w, "Roxanne/Roxanna", "Ann, Rose, Roxie");
        addDoc(w, "Rudolph/Rudolphus", "Dolph, Olph, Rolf, Rudy");
        addDoc(w, "Russell", "Russ, Rusty");
        addDoc(w, "Sabrina", "Brina");
        addDoc(w, "Samuel", "Sam, Sammy");
        addDoc(w, "Sandra", "Sandy");
        addDoc(w, "Sarah Jane", "Alydane, Dane");
        addDoc(w, "Sarah", "Sally, Sadie");
        addDoc(w, "Sebastian", "Seb");
        addDoc(w, "Selina/Celina", "Lena");
        addDoc(w, "Serena", "Rena");
        addDoc(w, "Serena", "Reni");
        addDoc(w, "Seymour", "Morey, See");
        addDoc(w, "Shannon", "Shanie");
        addDoc(w, "Shelton", "Shelly, Shel, Tony");
        addDoc(w, "Sheridan", "Dan, Danny, Sher");
        addDoc(w, "Shirley", "Lee, Sherry, Shirl");
        addDoc(w, "Sholette", "Lottie");
        addDoc(w, "Sidney", "Sid, Syd");
        addDoc(w, "Silas", "Si");
        addDoc(w, "Simon/Simeon", "Si, Sion");
        addDoc(w, "Sine", "Jean, Jane");
        addDoc(w, "Smith", "Smitty");
        addDoc(w, "Solomon", "Sal, Salmon, Saul, Sol, Solly, Zolly");
        addDoc(w, "Sophronia", "Sophie, Sophia");
        addDoc(w, "Stephen/Steven", "Steve, Steph");
        addDoc(w, "Submit", "Mitty");
        addDoc(w, "Sullivan", "Sully, Van");
        addDoc(w, "Susan/Susannah", "Sue, Sukey, Susie, Suz, Hannah");
        addDoc(w, "Sylvester", "Si, Sly, Sy, Syl, Vester, Vet, Vessie");
        addDoc(w, "Tabitha", "Tabby");
        addDoc(w, "Temperence", "Tempy");
        addDoc(w, "Tennessee", "Tennie, Tenny");
        addDoc(w, "Terence", "Terry");
        addDoc(w, "Thaddeus", "Thad");
        addDoc(w, "Theodore", "Ted, Teddy, Theo");
        addDoc(w, "Theophilus", "Theo");
        addDoc(w, "Theresa/Teresa", "Res, Terry, Tess, Tessie, Tessa, Thirza, Thursa, Tia, Tracy");
        addDoc(w, "Thomas", "Tom, Thom, Tommy, Tuck, Tucker");
        addDoc(w, "Timothy", "Tim, Timmy");
        addDoc(w, "Tobias", "Toby, Bias");
        addDoc(w, "Tryphena", "Phena");
        addDoc(w, "Uriah", "Riah");
        addDoc(w, "Valentine", "Felty, Feltie");
        addDoc(w, "Valerie", "Val");
        addDoc(w, "Vanessa", "Nessa, Essa, Vanna");
        addDoc(w, "Veronica", "Franky, Frony, Ron, Ronnie, Ronna, Vonnie");
        addDoc(w, "Victoria", "Vicky, Tori, Tory");
        addDoc(w, "Vincent/Vinson", "Vince, Vin, Vinnie");
        addDoc(w, "Virginia", "Ginger, Ginny, Jane, Jennie, Virgy, Virgie");
        addDoc(w, "Wallace", "Wally");
        addDoc(w, "Washington", "Wash");
        addDoc(w, "Wilber/Wilbur", "Will, Willie");
        addDoc(w, "Wilfred", "Will, Willie, Fred");
        addDoc(w, "Wilhelmina", "Mina, Willie, Wilma, Minnie, Wilha");
        addDoc(w, "Wilimena", "Mina, Willie, Wilma, Minnie");
        addDoc(w, "William", "Will, Willie, Bill, Billy, sometimes Bell or Bela");
        addDoc(w, "Winfield", "Win, Winny, Field");
        addDoc(w, "Winifred", "Winnie, Freddie, Winnet");
        addDoc(w, "Woodrow", "Wood, Woody, Drew");
        addDoc(w, "Zachariah", "Zach, Zachy, Zeke");
        addDoc(w, "Zachary", "Zach, Zachy, Zeke");
        addDoc(w, "Zebedee", "Zeb");
        addDoc(w, "Zedediah", "Zed, Diah, Dyer");
        addDoc(w, "Zephaniah", "Zeph");
        w.close();
    }

    /**
     *
     * @param querystr
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static boolean isNickName(String querystr) throws ParseException, IOException {
        return search(NICKNAME, NAME, querystr, 1) != null;
    }

    /**
     *
     * @param querystr
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static boolean isName(String querystr) throws ParseException, IOException {
        return search(NAME, NICKNAME, querystr, 1) != null;
    }

    /**
     *
     * @param querystr
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static boolean isHumanName(String querystr) throws ParseException, IOException {
        return isName(querystr) || isNickName(querystr);
    }

    /**
     *
     * @param inputField
     * @param ouputField
     * @param querystr
     * @param hitsPerPage
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static List<String> search(String inputField, String ouputField, String querystr, int hitsPerPage) throws ParseException, IOException {
        Query q = new QueryParser(inputField, analyzer).parse(querystr);
        List<String> result = null;

        // 3. search
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        if (hits.length > 0) {
            result = new ArrayList<String>(hits.length);
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                result.add(d.get(ouputField));
            }
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
        return result;
    }

    private static void addDoc(IndexWriter w, String name, String nickName) throws IOException {
        Document doc = new Document();
        doc.add(new TextField(NAME, name, Field.Store.YES));
        doc.add(new TextField(NICKNAME, nickName, Field.Store.YES));
        w.addDocument(doc);
    }
}
