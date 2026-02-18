import { Title } from "solid-start";
import { Image } from "@hope-ui/solid"
export default function Home() {
  return (
    <main>
      <Image
        htmlWidth="400px" htmlHeight="300px"
        borderRadius="$full"
        src="http://localhost:8090?filter=ip.edn"
        alt="Monkey D. Luffy"
        objectFit="cover"
      />
    </main>
  );
}
