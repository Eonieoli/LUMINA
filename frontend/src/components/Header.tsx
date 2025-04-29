import { BellIcon, LuminaLogo } from "@/assets/images";


export default function Header() {
    const scrollToTop = () => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    return (
        <>
            <div className="sticky top-0 bg-white pt-12 px-5 pb-2 flex justify-between z-30">
                <img onClick={scrollToTop} src={LuminaLogo} alt="로고" />
                <img src={BellIcon} alt="종" />
            </div>
        </>
    )
}