import { Route, Routes, useLocation } from "react-router-dom";
import {
  HomePage,
  SearchPage,
  DonatePage,
  RankingPage,
  MyPage,
  PostCreatePage,
  DonationDetailPage,
  DonationThanksPage,
  DonationSearchResultPage,
  DonationInputPage
} from "@/pages/mainPages";
import { Navbar } from "@/components";
import { useState, createRef, RefObject } from "react";
import { CSSTransition, SwitchTransition } from "react-transition-group";

export default function Main() {
  const location = useLocation(); // 기부처 페이지 컴포넌트 재마운트 용

  // 경로별 ref 맵 생성
  const [nodeRefs] = useState<Map<string, RefObject<HTMLDivElement | null>>>(() =>
    new Map([
      ["/", createRef()],
      ["/post", createRef()],
      ["/search", createRef()],
      ["/donate", createRef()],
      ["/ranking", createRef()],
      ["/mypage", createRef()],
      ["/donate/research", createRef()],
      ["/donate/:donationId", createRef()],
      ["/donate/:donationId/point", createRef()],
      ["/donate/thanks", createRef()]
    ])
  );

  const getNodeRef = (): RefObject<HTMLDivElement | null> => {
    // 정확한 경로 매칭
    if (nodeRefs.has(location.pathname)) {
      return nodeRefs.get(location.pathname)!;
    }

    // 부분 경로 매칭
    for (const [path, ref] of nodeRefs.entries()) {
      if (location.pathname.startsWith(path)) {
        return ref;
      }
    }

    // 기본값 fallback
    return nodeRefs.get("/")!;
  };

  return (
    <>
      <SwitchTransition mode="out-in">
        <CSSTransition
          key={location.pathname}
          nodeRef={getNodeRef()}
          classNames="fade"
          timeout={300}
          unmountOnExit
        >
          <div ref={getNodeRef()} className="flex justify-center items-center h-dvh">
            <div
              id="scrollable-container"
              className="relative h-dvh overflow-y-scroll w-full min-w-80 md:w-[600px] md:h-[90dvh] pb-20 md:pb-0 md:ml-20 md:border border-gray-300 md:rounded-xl"
            >
              <Routes location={location}>
                <Route index element={<HomePage />} />
                <Route path="post" element={<PostCreatePage />} />
                <Route path="search" element={<SearchPage />} />
                <Route path="donate" element={<DonatePage key={location.key} />} />
                <Route path="ranking" element={<RankingPage />} />
                <Route path="mypage/:userId?" element={<MyPage />} />
                <Route path="donate/research/:keyword" element={<DonationSearchResultPage />} />
                <Route path="donate/:donationId" element={<DonationDetailPage />} />
                <Route path="donate/:donationId/point" element={<DonationInputPage />} />
                <Route path="donate/thanks" element={<DonationThanksPage />} />
              </Routes>
            </div>
          </div>
        </CSSTransition>
      </SwitchTransition>
            <Navbar />
    </>
  );
}
