package org.choongang.global.services;

import lombok.RequiredArgsConstructor;
import org.choongang.global.Utils;
import org.choongang.global.repositories.SessionRepository;
import org.choongang.member.MemberUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository repository;
    private final MemberUtil memberUtil;
    private final Utils utils;

    public void save(String key, String value) {

    }
}
