package com.studyroom.member.member

import com.studyroom.member.ApplicationException
import java.util.UUID

class MemberNotFoundException(
    uid: UUID,
) : ApplicationException("MEMBER-404", "회원이 없다 : $uid")
