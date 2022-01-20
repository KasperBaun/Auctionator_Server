import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class Server {
    public SpaceRepository repository;
    public SequentialSpace auctionatorLobby;
    public String IpV4;
    public String lobbyURI;
    public Integer auctionCount;

    // Constructor
    public Server() throws URISyntaxException {

        auctionCount = 0;
        SpaceRepository repository = new SpaceRepository();
        this.repository = repository;

        SequentialSpace lobby = new SequentialSpace();
        repository.add("lobby", lobby);
        this.auctionatorLobby = lobby;

        // Set the URI of the lobby space
        String uri = "tcp://127.0.0.1:9001/?keep";

        // Open a gate
        URI myUri = new URI(uri);
        lobbyURI = "tcp://" + myUri.getHost() + ":" + myUri.getPort() +  "?keep" ;
        System.out.println("Opening repository gate at " + lobbyURI + "...\n");
        repository.addGate(lobbyURI);
    }

    public void listenForRequestToCreateAuction () throws InterruptedException {
        // Read request to create auction
        Object[] createRequest = auctionatorLobby.get(
                new ActualField("create"),
                new FormalField(String.class),      // Username
                new FormalField(String.class),      // Auction title
                new FormalField(String.class),      // Auction start-price
                new FormalField(String.class),      // End-time
                new FormalField(String.class),      // Description
                new FormalField(String.class)       // Image-URL
        );

        // Setup new thread with Auctioneer for handling the auction
        String username = createRequest[1].toString();
        String auctionTitle = createRequest[2].toString();
        String auctionPrice = createRequest[3].toString();
        String endTime = createRequest[4].toString();
        String description = createRequest[5].toString();
        String imageUrl = createRequest[6].toString();
        System.out.println("New auctionCreate request received from " + username);

        new Thread(new Auctioneer(
                auctionCount.toString(),        // AuctionID
                auctionatorLobby,
                username,                       // Username
                auctionTitle,                   // Auction title
                auctionPrice,                   // Auction price
                endTime,                        // End-time
                description,                    // Description
                imageUrl                        // Image-URL
        )).start();

        System.out.println("Creating auction " + auctionCount + " containing " + auctionTitle + " for " + username + " ...\n");

        // Increment auctionCount for next create
        this.auctionCount++;
    }

    public String getLocalMachineIPv4(){
        String localMachineIpV4;
        String port = "9001";
        try {
            localMachineIpV4 = "tcp://" + InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            // If it fails set variable to localhost
            localMachineIpV4 = "tcp://127.0.0.1:" + port;
        }
        this.IpV4 = localMachineIpV4;
        return localMachineIpV4;
    }

    public void fillServerWithDummyData() throws InterruptedException {
        auctionatorLobby.put(
                "create",
                "Simon Søhår",              // Username
                "Islandsk pony",             // Item name
                "50",                       // Start price
                "5",                       // End-time
                "Den islandske pony er efterkommer af de ponyer og heste, vikingerne havde med sig, da de bosatte sig på Island i niende og tiende århundrede. De medbragte heste var forskellige i udseende og farver, hvilket forklarer den store farvevariation i den islandske race.",               // Description
                "https://www.lundemoellen.dk/images/kr%C3%A6sen-hest2.jpg"
        );

        auctionatorLobby.put(
                "create",
                "Simon Hans Christian Fridolf Van Der Mark ",              // Username
                "Min farfars gamle majspibe",             // Item name
                "150",                    // Start price
                "10",                     // End-time
                "En majspibe er en billig og naturlig pibe, hvor selve pibehovedet er lavet af majskolbe mens mundstykket er lavet af træ og plastik",               // Description
                "https://norrebroskiosk.dk/wp-content/uploads/2018/03/majspiber-ny.jpeg"
        );

        auctionatorLobby.put(
                "create",
                "Kristoffer Von Schnitzel",              // Username
                "Sage Espresso Maskine",             // Item name
                "2500",                    // Start price
                "10",                     // End-time
                "Sælger min gamle sage espressomaskine, den virker fint og der medfølger en god pose kaffebønner ved hurtig handel",               // Description
                "https://royaldesign.se/image/1/sage-barista-pro-espressomaskin-1?w=168&quality=80"
        );

        auctionatorLobby.put(
                "create",
                "Kasper Kristian Kristoffer Van Der Ocean",              // Username
                "Shark Cookie Mug",             // Item name
                "149",                    // Start price
                "15",                     // End-time
                "Fantastisk krus jeg købte på en charterferie i florida. Kaffe og kager lige ved hånden - what's not to like?",               // Description
                "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxIQEhAPEBIVEBUQEg8QDxAQEA8PDxAQFREWFhUWFhUYHSggGBomGxUVITEhJSkrMC4uFx8zODMtNygtLisBCgoKDg0OGhAQGC0lICUrLi0tLS0tLS0tLS0wLS0tKy0tLSstLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLTEtLf/AABEIAOEA4QMBIgACEQEDEQH/xAAcAAEAAgMBAQEAAAAAAAAAAAAABAUCAwYBBwj/xAA+EAACAQIEAwYDBQYEBwAAAAAAAQIDEQQFITESQVEGEyJhcZEygaEUQmKxwQcVUnKC0SNTkvEWJDNDorLh/8QAGQEBAAMBAQAAAAAAAAAAAAAAAAECAwQF/8QAJBEBAQACAQQCAgMBAAAAAAAAAAECEQMSITFRBBNBYSJxgVL/2gAMAwEAAhEDEQA/APuIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMJ1YreSXq0jRPMaK3qR90RuJ0lAhrNaH+bH3NsMbTltOL/qQ6p7NVvB4nfY9JQAAAAAAAAAAAAAAAAAAAAAAAAAAAaq+IjBXk0kZVqnDFyfJNnI4uvKo3KTv06JGXJydK+GHUscb2itdU43/ABS/sUeLzitLebXktDRWmlp10Xmcl257SLB0uGGtaqmqa/hXOb9Dm6ss7pt044xpzT9oNGnLh/xKjX/USVnTd7Wd+fkRcs7brE4qOHhTfdz0jUbtO9r6x6fM+Wtym23eTbbk92292z6F+y/B07VqrSdSE+BS34YuPI1zwxxxVxytun0BGSb6mKMkczZIoYupD4ZNejZb4PtFUjZT8S89yjRnFFplZ4VuMrucFm1Ors7PoyefO1pqtC5y3O507Rn4o/VG+HP/ANMcuP06sGjCYuFVXg79VzRvOiWXwyAASAAAAAAAAAAAAAAAAAAA1VY8cZQ6pr3RwfaDHxwlKpVqp/4dlwxV5Sm3ZRXm2zua90+Jf7nLdvuz08fQSo1O6tOLr2jxTlTje6j0ltqc/JOr/GmF0+M4jE4nH1XWnQr1FTuqcMNVjSjh5fzP4p9fYiUOz+Pxs1SrKpCnTldVMTwyqRT5KS1l+R9UweBp4enGjSjwwgrJc31bfNsykY/b6jboc9lnZTDYdLghefDKLqSbcnfd9DLIsklh6lerOopus4u0YqEVwq17dS8Z5Yp1VfUZJGQsCo9Rsia0zNAbkZRMIm2nBvSKcn0Sbf0CG2hVlB8UHZr6nWZVje+he1mnaS8yiwmS1Jay/wANfi39v7l7gaVOkuCDu29Xu2zo4ZlLu+GPJZU0AHWxAAAAAAAAAAAAAAAAAABqrEaUnF8S+a6jG4tR1WvUg/bWznyym15EbM8nVW9Sg1d6ypvTXy6ehzmJoSg7Ti4PpJW/3L+vVcXxRdvQ8/fc0rTiprpJGWWMrTHKxzYL+WZYd/Fho38lFHn2/D8sLH+qzK/X+1/s/SkRspUJT+CMpfyxcvyLqGbpfBQpx9IoynnlZ7Wj6In657R9n6QKORYiX/b4fOTUfpuTqXZxrWrVjDyjr9XY0VMdVlvOXonZGrV7tv1ZaY4z8K3PJaRo4Sl1qvzd1+iNrzaytSgoLySKmKN8EWl9KXv5SJYic/ik35E/Lo6or6aLXLo6kyd0VZAA6VAAAAAAAAAAAAAABpqV0tWVWOzhR0XstzO8kiZFzKoluytzHMqai0pa8vMr6OCr1/FJ93F7XvxNehvn2ahb45X6tKxW3LKeFu0UeIzPXfR7m+hWuiuzLAunJxa1X1JmVQ4nGPp7GGl0qtFpa81cg1EWuYLX5Mq6iLIaLBGTQSA9SMkeIzQQ9SMoldmubU8NFym9dlGKvKUnsklu30KVZ5jX440qMVuqM5z7xro5pNKXuvMtMbUbdfEkQRSZFnlPE3ik6dSGlShUsqkH+q6NaMvYojWkttNFvl6Kqmi4wK0LYeUVKAB0KAAAAAAAAAAAEfF1eFEgo80xG5nyXUTIhZhjunyJuS5Ta1aqryesYv7vm/Mh5JhO+qOpL4YcuTlyX6nUFePHferZX8AANlFbm+WxrJNvha+8lf3IeXYNU/Xa/kXOI2ISMMp3XnhWZhuvmVlQtMx3XzKuoZ3ys0nqQaPUEPUVuc5tGhCTbtZXb34emnNvkjbmmOVGL1V7N3e0Vzkzj8NxYmarzv3cXfDwd7yf+bJdf4VyWu70vjjvui1lhr1Kqq12lUkpOjRk05U4c5Nc5u6u+W3W9rAi4bCRg5zWsqjvKT1k1yjflFckSomqiA4d/Jvgnhq1Gzp1rRejva0k2pxdtYs6rsxnbrXoVkoV6SXHFfDOOynDrF/TZlRE1YyhK8K1LSrRblSeykvvU5fhktPZ8iLNplfQqSLnBrwnNZDmEcRShVj95ap7xktGn5ppp+h0+GXhRXCd01tABsqAAAAAAAAAADGb0ZyeZ1bt+bOqxD8MvRnLU6fHXhHrJX9Fq/yMeTvVsXRZXhu7pxjzteX8z1f9vkSwDWTSoACRqrrQiMm1loQ5mOflaKrMN18ysmWeP3XzK6ZlV2k14isqcXJ8uXV9DbY5LtRmcpONGk/HNuNPmo2+Oo/KK+tlzLYzdRbpX42q8ZVlB604Nd8+VSa1VJfhWjl8l1LJIj4TDRpQjThtHrq2922+bbu36kg2ZskZo1o2RCGyJtiaom2JIl9mK/c4mdL7lZd9BclNNRqJe8ZfOR9JorRHybGYnuFDE2cu4mpOMbJuEk4SX/kn8j6nluIVWlSqx2qQhON97SimRJ3WSQAXQAAAAAAAAAADXiV4ZehzmVr/AJmP9f8A6s6WaumvI5nCvgxUL83KPvFoyz8xaeHUAA1VAABhW2Icjfi6yitWQaeIUtjHOza0V+OevuQJk7GvxFbiq0acZTm+GME5Sb2UUrtmSyuznGqnBq9rptvbhjzZx+URdRyxUlrVsqSf3KCfh9HL4n6roV2L7R/vGs8PTg405O9SpJ2k6UXdpR5KWkd+Z0cUbYzUUt29MkeJGSRZD2JsijFI1Tx9KL4XNOX8Ebzqf6Y3YNJsUboRObqdraEZOCjUk1Lu34FBKd7WfG1b5krMO0P2apClWozhKaUkuOjK0W7JyalZJkbW6auMfhuOjWj1py97XR9My6gqdKlTW0KcIL0UUj5t2ezKli6n2e04OV4eJR15Nppu/qfUUraFse6LLPL0AFkAAAAAAAAAAAHL59TdOoprk1JeqZ1BBzfB97BpbrVf2KZzcTL3ScLXVSEZraSTNpymS5k6DdKd+FvT8LOihjoPmRjySzum4pJqr1VFNvoYzxCW2pS5njOVyM+STwTHaszTHPV3McDiW7SIGNq3Msvnoc7Vb1p3bZz/AGph3lCpRvbvYSg30urF3yv1Oe7UTlCEqltIQlJv0TZfHypXA9iMudONapNLic3SVndWg7Oz/mv7HWxic7gM1oYelSpzqJzUIOSinJuctXqtFq+ZBzXtLWc50qXDRjDSc1KFSblbZPWKtztf1NblJNp4+HPPKYyd3U4vGU6SvOSjfZbyl6RWrNeRYr7c6qoSjTVJqM3UTdTVXuqaa031b5bHH4jtHCVD7N3UY1HKMq2Ii+KVaEbtKTd5X4uHnbQqI42cHPuZypucHSquPh46ct4t/qZ3k7uifE/hbbNy60uO0GP7+rOFKrUlRp3hxuXD300/FJKKSUVstNdy3yztxLDYaeHdKKrKPDQrQhGMJN6cVRK2sVr52OPjKyt8KS0S2sbKFPilZX21umjK8l3t05cXB9cxm7fft6pJR4LOd/ib3k3u35kSeYSUnGpxviSSm3edkrLfklyLTuOHdq+rS+ehv/d8KkZd4nZK7lFXcfNFJnrytyZ5Z66dTU1P6dN+xtXxicm7rZy3leLs11PvZ8P7G8OHdGcHdRcZKW+nLXpuvmfbKFaM4qUGpJq6aaaZt8blmVyxcPyePp1fbYADrcoAAAAAAAAAAAAAqM3yVVfHDwy59Jf2Zz9SNahpKLS89V8mduDPLjlWmVjhHmU3pdiGGr1fhhJ+drL3Z3PCun0MKlVIr9Xup63HSyCSs6slH8MdX7mWHwsYbK/S+pbZnVumVEK9ymWMiZbWvNcRwQlJfdi2vY/PuYY+rXnOpUqSk5NvWTslfZLZI+85zrTmusX+R8GnQ1a6Nr6jFaIUk7aETubLxbctf0LfuDCWHWzV0WuSdbasFDgp97Zu+iW2l7G/B41SlaULdGtSxwuCU6aprVapL53PMFlUIye911drHPc8bvbpx48prXhnKi3G9OKbeya26kWvjO5l4lxS00Vla3UvVStbXitz02/UxxmT0aydSScZJptxbUtdDLHKb/k1zxutxXYTAxxce8i3TnfVrild328jov3dej3XFKLs1JppJ36+RWZZTjSl4F4Obi3JylzuXdbEwU40pSUXUXhutW9ynJnd9lsMJruqsHm0Kc44SCl4ajjduycn+Su37H1HsFnPDVhhWuFVeOylu5wi5Xj1Vkz5bjsgrfaIVrKrFSjOXCowbSd/c+i9jVx4nDylG006krPeC4JaX2ejHVjOTDLFXKW8eWNfUQAeu8oAAAAAAAAAAAAxlIDIwlUSNNWqQ6tUi0Sa2KIFfEmupI0TKWpaMRUuU+Jk4PiXzXUt5wItfD3K2JiNUkpw05o+TZtlMoV6ito5OUfRu59Mq8VFtpXi91zXoRK8KFfdq/npJfIyssaY185WBfQ1VMJbkfQamSw5MjVcnprcja+44nL5d3Kz2f5lnUocU1JJaWtLnfnfp/8ASXjsHh47zivK6bK94yEeK0m07LVbyvZPUx5ML5jp4uSeK3um3pa++qtqvUzTdnCmtXF+K/O213uaa0pqL7q3Eotpcm9zDIe84anfJqXHxRjLdX6fMx1222uXfTncvwdec7XlC07Scrx28ufodLjcsVRwlxS4ouOsknazvp0JkaM21a26vxKStp5EmFGSavq1fbkrck/Owz5bbsx45jNJOCxTUXF+Na+p1f7JYTrweMqRtZSpxVrPV7/6be5QZVgO+cYapOycpJbc3Y+sZNgadCjClSjwxitPN82zT4vFMsuqzww+Ty6nTPynAA9R5wAAAAAAAAAeMDGUjRUkbJGqSIQ0zNMoklxMHEqIrpmEoEpxMJQCUOcDTOBNlE1SpkJVtaimU2NyaE+R0s6RonQIsJXGVcgttKS9JNEGtkC+83L1bZ3c8MR6uBuV6Vup8/qZPBbIr8Vl9uR9Bq5W9dCDWyhvkRpeZOCVWUNGrpfJ2JdHHwtbWP8AMro6WrkF+X0NH/DN/u/Qxy4ZW2PPYraePhZ+Ja9FK+1iTh6vG7pfSyLXC9k5P7v0OnynsjazloVx+Nitl8moXZzBSlJaH0OhDhikaMFgIUklFEpHbhh0xx5ZdVeniR6C6oAAAAAAAAYsyAGtowaNtjxohDS4mLib2jHhGkI7iYOJKcTBxISjOmYOmS3ExcQIbpGLok3gPO7GjaA6B59nJ/dnvdkaTtX/AGUyjg10LBUzZGmNG1fHAR6Einl8ehNjAzSJ0baqeHiuRuSALAAAAAAAAAAAAAAAADxhgBFeM8PAEPDxnoKjFmLAA8ABKQ9R6ABtQAGSPQCUgAAAAAAAAAAAAAAAP//Z"
        );

        auctionatorLobby.put(
                "create",
                "Dennis Dingo",              // Username
                "Mit Kæle-egern Niels",             // Item name
                "85",                    // Start price
                "20",                     // End-time
                "Sælger mit kæle-egern Niels da jeg skal flytte til Australien. Han er blød, sød og selskabelig, dog kan han godt finde på  at bide hvis du glemmer at give ham nødder.",               // Description
                "https://styles.redditmedia.com/t5_3qzv06/styles/profileIcon_7zhvolvs8wq71.jpg?width=256&height=256&crop=256:256,smart&s=627e2372839bcbdf48a7dc00511202ae2e47001e"
        );
    }
}





